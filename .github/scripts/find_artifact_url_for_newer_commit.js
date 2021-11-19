module.exports = async ({context, core, github}) => {
	core.info("Finding current snapshot ...")
	const {snapshotDate, snapshotSha} = await find_current_snapshot(context, github)

	core.info(`Current snapshot: ${snapshotSha || "NONE"}`)

	core.info("Finding commits ...")
	const commits = await find_commits_since_snapshot(snapshotDate || "1970-01-01T00:00:00Z", context, github)

	for (let commit of commits) {
		if (commit.oid === snapshotSha) {
			core.notice(`${commit.url} - snapshot is already at the newest possible commit`)
			return null
		}

		if (!commit.statusCheckRollup) {
			core.info(`${commit.url} - ignoring commit with no status check`)
			continue
		}

		if (commit.statusCheckRollup.state !== "SUCCESS") {
			core.info(`${commit.url} - ignoring ${state} commit`)
			continue
		}

		for (let suite of commit.checkSuites.nodes) {
			const run = suite.workflowRun;
			if (run && run.workflow.name === "CI" && suite.branch.name === "master") {
				core.info(`${commit.url} - finding artifact from workflow run ${run.url} ...`)
				const url = await find_artifact_url_from_workflow_run(run.databaseId, context, github)
				if (url) {
					core.notice(`Updating to ${url} from ${commit.url}`)
					return {
						sha: commit.oid,
						url
					}
				}
			}
		}

		core.info(`${commit.url} - ignoring commit with no suitable artifacts`)
	}

	// We could look at more commits by paging the find_commits_since_snapshot() query
	// but this will probably never be relevant so not implemented
	core.warning(`No suitable artifact from ${commits.length} newest commits`)
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
				owner: "plantuml", //context.repo.owner,
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
		query($owner: String!, $name: String!, $snapshotDate: GitTimestamp!) {
		  repository(owner: $owner, name:$name) {
			object(expression: "master") {
			  ... on Commit {
				history(first: 100, since: $snapshotDate) {
				  edges {
					node {
					  oid
					  url
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
				owner: "plantuml", //context.repo.owner,
				name: context.repo.repo,
				snapshotDate,
			}
	)
	return response.repository.object.history.edges.map(edge => edge.node)
}

async function find_artifact_url_from_workflow_run(runId, context, github) {
	const response = await github.rest.actions.listWorkflowRunArtifacts({
		// ...context.repo,
		owner: "plantuml",
		repo: "plantuml",
		run_id: runId,
	});
	const artifact = response.data.artifacts.find(a => a.name.endsWith("-jarsX"));
	return artifact ? artifact.archive_download_url : null
}

// https://api.github.com/repos/matthew16550/plantuml/actions/runs/1458211290/artifacts

// https://api.github.com/repos/plantuml/plantuml/releases/tags/snapshot

// https://api.github.com/repos/plantuml/plantuml/check-suites/4389169301
