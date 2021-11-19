module.exports = async ({context, core, github}) => {
	core.info("Finding current snapshot ...")
	const {snapshotDate, snapshotSha} = find_current_snapshot(context, github)

	if (snapshotSha)
		core.info(`Current snapshot: ${snapshotSha}`)
	else
		core.info("There is no current snapshot")

	core.info("Finding commits since snapshot ...")
	const commits = find_commits_since_snapshot(snapshotDate || 0, context, github)

	for (let commit of commits) {
		if (commit.oid === snapshotSha) {
			core.info("Snapshot already at newest possible commit")
			return null
		}

		if (commit.statusCheckRollup.state !== "SUCCESS") {
			core.info(`Ignoring ${state} commit ${sha}`)
			continue
		}

		for (let suite of commit.checkSuites.nodes) {
			const run = suite.workflowRun;
			if (run && run.workflow.name === "CI" && suite.branch.name === "master") {
				core.info(`Finding artifact from workflow run ${run.url} ...`)
				const url = await find_artifact_url_from_workflow_run(run.databaseId, context, github)
				if (url) {
					core.info(`Using ${url} for ${sha}`)
					return {
						sha: commit.oid,
						url,
					}
				}
			}
		}

		core.info(`No suitable artifacts for commit ${sha}`)
	}

	// We could look at more commits by paging the find_commits_since_snapshot() query
	// but this will probably never happen so not implemented
	core.info("No suitable artifact from the 100 newest commits")
	return null
}

async function find_current_snapshot(context, github) {
	const response = await github.graphql(`
		query ($owner: String!, $name: String!) {
		  repository(owner: $owner, name: $name) {
			release(tagName: "snapshot") {
			  tagCommit {
				committedDate
				oid
	  	} } } } `,
			{
				owner: context.repo.owner,
				name: context.repo.repo,
			}
	)
	const release = response.repository.release
	return {
		snapshotDate: release ? release.tagCommit.committedDate : null,
		snapshotSha: release ? release.tagCommit.oid : null,
	}
}

async function find_commits_since_snapshot(snapshotDate, context, github) {
	const response = await github.graphql(`
		query($owner:String!, $name:String!, snapshotDate:DateTime!) {
		  repository(owner:$owner, name:$name) {
			object(expression: "master") {
			  ... on Commit {
				history(first: 100, since: $snapshotDate) {
				  edges {
					node {
					  oid
					  statusCheckRollup {
						state
					  }
					  checkSuites(first: 100) {
						nodes {
						  branch {
							name
						  }
						  workflowRun {
							databaseId
							url
							workflow {
							  name
		} } } } } } } } } } }`,
			{
				owner: context.repo.owner,
				name: context.repo.repo,
				snapshotDate,
			}
	)
	return response.repository.object.history.edges.map(edge => edge.node)
}

async function find_artifact_url_from_workflow_run(runId, context, github) {
	const response = await github.rest.actions.listWorkflowRunArtifacts({
		...context.repo,
		run_id: runId,
	}); // TODO error throws?
	const artifact = response.artifacts.find(a => a.name.endsWith("-jars"));
	return artifact ? artifact.archive_download_url : null
}

// https://api.github.com/repos/matthew16550/plantuml/actions/runs/1458211290/artifacts

// https://api.github.com/repos/plantuml/plantuml/releases/tags/snapshot

// https://api.github.com/repos/plantuml/plantuml/check-suites/4389169301
