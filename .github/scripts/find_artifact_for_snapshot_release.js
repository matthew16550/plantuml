module.exports = async ({context, core, github}) => {
	core.info("Finding current snapshot ...")
	// const {snapshotDate, snapshotSha} = await find_current_snapshot(context, github)
	const snapshotDate = null
	const snapshotSha = null
	core.info(`Current snapshot: ${snapshotSha || "NONE"}`)

	core.info("Finding commits ...")
	const commits = await find_commits_since_snapshot(snapshotDate || "1970-01-01T00:00:00Z", context, github)

	for (let commit of commits) {
		core.info(`\nConsidering ${commit.url}`)

		if (commit.oid === snapshotSha) {
			core.notice(`Snapshot is already at the newest possible commit\n${commit.url}`)
			return
		}

		if (!commit.statusCheckRollup) {
			core.info(`Ignoring commit with no status check`)
			continue
		}

		if (commit.statusCheckRollup.state !== "SUCCESS") {
			core.info(`Ignoring ${state} commit`)
			continue
		}

		for (let suite of commit.checkSuites.nodes) {
			const run = suite.workflowRun;
			if (run && run.workflow.name === "CI" && suite.branch && suite.branch.name === "master") {
				core.info(`Finding artifact from ${run.url} ...`)
				const artifact_name = await find_artifact_name_from_workflow_run(run.databaseId, context, github)
				if (artifact_name) {
					core.notice([
						`Updating to : ${artifact_name}`,
						`Run         : ${run.url}`,
						`Commit      : ${commit.url}`,
					].join("\n"))

					core.setOutput('artifact_name', artifact_name);
					core.setOutput('sha', commit.oid);
					core.setOutput('workflow_run_id', run.databaseId);
					return
				}
			}
		}

		core.info(`Ignoring commit with no suitable artifacts`)
	}

	// We could look at more commits by paging the find_commits_since_snapshot() query
	// but this will probably never be relevant so not implemented
	core.warning(`No suitable artifact from ${commits.length} newest commits`)
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
				owner: context.repo.owner,
				name: context.repo.repo,
				snapshotDate,
			}
	)
	return response.repository.object.history.edges.map(edge => edge.node)
}

async function find_artifact_name_from_workflow_run(runId, context, github) {
	const response = await github.rest.actions.listWorkflowRunArtifacts({
		...context.repo,
		run_id: runId,
	});
	const artifact = response.data.artifacts.find(a => a.name.endsWith("-jars"));
	return artifact ? artifact.name : null
}
