const COMPLETED_STATUS = "COMPLETED"

class BuildScheduler {
	constructor(args) {
		this.branchRef = args.branchRef
		this.markRef = args.markerRef
		this.workflowPath = args.workflowPath
		this.workflowInputs = args.workflowInputs
		this.context = args.context
		this.core = args.core
		this.github = args.github
	}

	async schedule() {
		const workFlowId = await this.findWorkflowId()

		const markedCommit = await this.findMarkedCommit()

		if (!markedCommit) {
			this.core.setFailed(`Mark '${this.markRef}' not found'`)
			return
		}

		this.core.notice(`Mark '${this.markRef}' points to ${markedCommit.url}`)

		const runs = await this.findWorkflowRuns(workFlowId, markedCommit.sha)

		if (runs.length === 0) {
			this.core.notice(`'${this.workflowPath}' has no runs for the marked commit`)
			await this.triggerRun(workFlowId, markedCommit)
			return
		}

		this.core.notice(
				`'${this.workflowPath}' has these runs for the commit:\n`
				+ workflowRuns.map(run => `${run.status.padEnd(11)} ${run.url}`).join("\n")
		)

		if (workflowRuns.some(run => run.status !== COMPLETED_STATUS)) {
			this.core.notice(`Some run(s) not ${COMPLETED_STATUS} so the scheduler will do nothing`)
			return
		}

		const nextCommit = await this.findCommitAfter(markedCommit.sha)

		if (nextCommit) {
			await this.triggerRun(workFlowId, nextCommit)
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

	async findWorkflowId() {
		this.core.info(`Finding '${this.workflowPath}' workflow ...`)
		const opts = github.rest.actions.listRepoWorkflows.endpoint.merge({
			...context.repo,
		})
		const workflows = await github.paginate(opts)
		for (const workflow of workflows) {
			if (workflow.path === WORKFLOW_PATH) {
				this.core.info(`The workflow is ${workflow.html_url}`)
				return workflow.id
			}
		}
		throw new Error(`Workflow not found: '${WORKFLOW_PATH}'`)
	}

	async findWorkflowRuns(workflowId, sha) {
		this.core.info(`Finding workflow runs for '${sha}' ...`)
		const result = []
		for await (const suite of this.queryCheckSuites(sha)) {
			if (suite.workflowRun && suite.workflowRun.workflow.databaseId === workflowId) {
				workflowRuns.push({
					status: suite.status,
					url: suite.workflowRun.url
				})
			}
		}
		return result
	}

	async* queryCheckSuites(sha) {
		let commit = null
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
								  }
								}
							  }
							}
					} } } } } }`,
					{
						...this.context.repo,
						sha,
						cursor: commit ? commit.pageInfo.endCursor : null,
					}
			)
			console.log(response)
			commit = response.repository.object
			if (commit)
				yield* commit.checkSuites.nodes
		}
		while (commit && commit.pageInfo.hasNextPage)
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
			const response = await github.graphql(`
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
			console.log(response)
			if (response.repository.object == null)
				throw new Error(`No commit found for '${this.branchRef}'`)
			history = response.repository.object.history
			yield* history.nodes
		}
		while (history && history.pageInfo.hasNextPage)
	}

	async triggerRun(workFlowId, commit) {
		this.core.info(`Setting '${this.markRef}' to ${commit.sha} ...`)

		await this.github.rest.git.updateRef({
			...this.context.repo,
			ref: this.markRef,
			sha: commit.sha,
			force: true,
		});

		this.core.notice(`Triggering run for ${commit.url} ...`)

		await this.github.rest.actions.createWorkflowDispatch({
			...this.context.repo,
			workflow_id: workFlowId,
			ref: this.markRef,
			inputs: this.workflowInputs,
		})
	}
}

module.exports = async (args) => {
	await new BuildScheduler(args).schedule()
}
