const RELEASE_BRANCH = "snapshot-release-2"

module.exports = async ({context, core, github}) => {
	core.info("Finding current snapshot ...")
	const {snapshotDate, snapshotSha} = await findCurrentSnapshot(context, github)

	core.info(`Current snapshot: ${snapshotSha || "NONE"}`)

	core.info(`Finding commits ...`)
	const commits = await findCommitsSinceSnapshot(snapshotDate || "1970-01-01T00:00:00Z", context, github)

	for (let commit of commits) {
		core.info(`\nConsidering ${commit.url}`)

		if (commit.oid === snapshotSha) {
			core.notice(`Snapshot is already at the newest possible commit\n${commit.url}`)
			return
		}

		if (!commit.statusCheckRollup) {
			core.info(`Ignore because no status check`)
			continue
		}

		if (commit.statusCheckRollup.state !== "SUCCESS") {
			core.info(`Ignore because ${commit.statusCheckRollup.state}`)
			continue
		}

		for (let suite of commit.checkSuites.nodes) {
			const run = suite.workflowRun;
			if (run && run.workflow.name === "CI" && suite.branch && suite.branch.name === RELEASE_BRANCH) {
				core.info(`Finding artifact from ${run.url} ...`)
				const artifactName = await findArtifactNameFromWorkflowRun(run.databaseId, context, github)
				if (artifactName) {
					core.notice([
						`Updating to : ${artifactName}`,
						`Run         : ${run.url}`,
						`Commit      : ${commit.url}`,
					].join("\n"))

					core.setOutput("artifact_name", artifactName);
					core.setOutput("sha", commit.oid);
					core.setOutput("workflow_run_id", run.databaseId);
					return
				}
			}
		}

		core.info(`Ignore because no suitable artifacts`)
	}

	// We could look at more commits by paging the findCommitsSinceSnapshot() query
	// but this will probably never be relevant so not implemented
	core.setFailed(`No suitable artifact from ${commits.length} newest commits`)
}

async function findCurrentSnapshot(context, github) {
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

async function findCommitsSinceSnapshot(snapshotDate, context, github) {
	const response = await github.graphql(`
		query($owner: String!, $name: String!, $headCommit: GitObjectID!, $snapshotDate: GitTimestamp!) {
		  repository(owner: $owner, name:$name) {
			object(oid: $headCommit) {
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
				headCommit: process.env.GITHUB_SHA,
				snapshotDate,
			}
	)
	return response.repository.object.history.edges.map(edge => edge.node)
}

async function findArtifactNameFromWorkflowRun(runId, context, github) {
	const response = await github.rest.actions.listWorkflowRunArtifacts({
		...context.repo,
		run_id: runId,
	});
	const artifact = response.data.artifacts.find(a => a.name.endsWith("-jars"));
	return artifact ? artifact.name : null
}
