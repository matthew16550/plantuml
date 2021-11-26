const COMPLETED_STATUS = "COMPLETED"

class BuildScheduler {
	constructor(args) {
		if (!args.branchRef.startsWith("refs/heads/"))
			throw new Error("branchRef must start with 'refs/heads/'")
		if (!args.markRef.startsWith("refs/"))
			throw new Error("markRef must start with 'refs/'")
		
		this.branchRef = args.branchRef
		this.markRef = args.markRef
		this.workflowPath = args.workflowPath
		this.workflowInputs = args.workflowInputs
		this.context = args.context
		this.core = args.core
		this.github = args.github
	}

	async schedule() {
		const workflow = await this.findWorkflow()
		
		this.core.info(`Target Workflow : ${workflow.html_url}`) // TODO notice
		this.core.info(`State           : ${workflow.state}`)
		
		if (workflow.state !== "active") {
			this.core.warning("Workflow not active so the scheduler will do nothing")
			return
		}

		const markedCommit = await this.findMarkedCommit()

		if (!markedCommit) {
			this.core.setFailed(`Mark not found: '${this.markRef}'`)
			return
		}

		this.core.notice(`Mark '${this.markRef}' points to ${markedCommit.url}`)

		const runs = await this.findWorkflowRuns(workflow.id, markedCommit.sha)

		if (runs.length === 0) {
			this.core.notice(`'${this.workflowPath}' has no runs for the marked commit`)
			await this.triggerRun(workflow.id, markedCommit)
			return
		}

		this.core.notice(
				`'${this.workflowPath}' has these runs for the commit:\n`
				+ runs.map(run => `${run.status.padEnd(11)} ${run.url}`).join("\n")
		)

		if (runs.some(run => run.status !== COMPLETED_STATUS)) {
			this.core.notice(`Some run(s) not ${COMPLETED_STATUS} so the scheduler will do nothing`)
			return
		}

		const nextCommit = await this.findCommitAfter(markedCommit.sha)

		if (nextCommit) {
			await this.triggerRun(workflow.id, nextCommit)
			return
		}

		this.core.notice("No newer commits so the scheduler will do nothing")
	}

	async findMarkedCommit() {
		this.core.info(`Finding commit for mark '${this.markRef}' ...`)
		const response = await this.github.graphql(`
				query($owner: String!, $repo: String!, $ref: String!) {
				  repository(owner: $owner, name: $repo) {
					object(expression: $ref) {
					  ... on Commit {
						sha: oid
						url
				} } } }`,
				{...this.context.repo, ref: this.markRef}
		)
		return response.repository.object
	}

	async findWorkflow() {
		this.core.info(`Finding '${this.workflowPath}' workflow ...`)
		const opts = this.github.rest.actions.listRepoWorkflows.endpoint.merge({
			...this.context.repo,
		})
		const workflows = await this.github.paginate(opts)
		const result = workflows.find(w => w.path === this.workflowPath)
		if (!result)
			throw new Error(`Workflow not found: '${this.workflowPath}'`)
		return result
	}

	async findWorkflowRuns(workflowId, sha) {
		this.core.info(`Finding workflow runs for '${sha}' ...`)
		const result = []
		for await (const suite of this.queryCheckSuites(sha)) {
			if (suite.workflowRun && suite.workflowRun.workflow.databaseId === workflowId) {
				result.push({
					status: suite.status,
					url: suite.workflowRun.url
				})
			}
		}
		return result
	}

	async* queryCheckSuites(sha) {
		let object = null
		do {
			let response = await this.github.graphql(`
					query($owner: String!, $repo: String!, $sha: GitObjectID!, $cursor: String) {
					  repository(owner: $owner, name: $repo) {
						object(oid: $sha) {
						  ... on Commit {
							checkSuites(first: 2, after: $cursor) {
							  pageInfo { hasNextPage, endCursor }
							  nodes {
								status
								workflowRun {
								  url
								  workflow {
									databaseId
					} } } } } } } }`,
					{
						...this.context.repo,
						sha,
						cursor: object ? object.checkSuites.pageInfo.endCursor : null,
					}
			)
			object = response.repository.object
			if (object)
				yield* object.checkSuites.nodes
		}
		while (object && object.checkSuites.pageInfo.hasNextPage)
	}

	async findCommitAfter(currentSha) {
		this.core.info(`Finding commit after ${currentSha} ...`)
		let nextCommit = null
		for await (const commit of this.queryBranchHistoryNewestFirst()) {
			if (commit.sha === currentSha)
				return nextCommit
			else
				nextCommit = commit
		}
	}

	async* queryBranchHistoryNewestFirst() {
		let history = null
		do {
			const response = await this.github.graphql(`
					query($owner: String!, $repo: String!, $branchRef: String!, $cursor: String) {
					  repository(owner: $owner, name: $repo) {
						object(expression: $branchRef) {
						  ... on Commit {
							history(first: 100, after: $cursor) {
							  pageInfo {
								hasNextPage
								endCursor
							  }
							  nodes {
								sha: oid
								url
					} } } } } }`,
					{
						...this.context.repo,
						branchRef: this.branchRef,
						cursor: history ? history.pageInfo.endCursor : null,
					}
			)
			if (response.repository.object == null)
				throw new Error(`No commit found for '${this.branchRef}'`)
			history = response.repository.object.history
			yield* history.nodes
		}
		while (history && history.pageInfo.hasNextPage)
	}

	async triggerRun(workflowId, commit) {
		this.core.info(`Setting '${this.markRef}' to ${commit.sha} ...`)

		await this.github.rest.git.updateRef({
			...this.context.repo,
			ref: this.markRef.substring("refs/".length),
			sha: commit.sha,
			force: true,
		});

		this.core.notice(`Triggering run for ${commit.url} ...`)

		await this.github.rest.actions.createWorkflowDispatch({
			...this.context.repo,
			workflow_id: workflowId,
			ref: this.markRef,
			inputs: this.workflowInputs,
		})
	}
}

module.exports = async (args) => {
	await new BuildScheduler(args).schedule()
}
