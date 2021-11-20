
module.exports = async ({context, core, github}) => {
	const RELEASE_BRANCH = "snapshot-release-2"
	core.info("Finding current snapshot ...")

	const currentSnapshot = (
			await github.graphql(`
				query ($owner: String!, $repo: String!) {
				  repository(owner: $owner, name: $repo) {
					release(tagName: "snapshot") {
					  tagCommit {
						committedDate
						oid
				} } } } `, context.repo
			)
	).repository.release

	const snapshotSha = currentSnapshot ? currentSnapshot.tagCommit.oid : null

	core.info(`Current snapshot: ${snapshotSha || "NONE"}`)

	core.info(`Finding newest commits ...`)

	const commits = (
			await github.graphql(`
				query($owner: String!, $repo: String!, $head: String!, $since: GitTimestamp!) {
				  repository(owner: $owner, name: $repo) {
					object(expression: $head) {
					  ... on Commit {
						history(first: 100, since: $since) {
						  edges {
							node {
							  oid
							  url
							  checkSuites(first: 100) {
								nodes {
								  branch {
									name
								  }
								  conclusion
								  workflowRun {
									databaseId
									runNumber
									url
									workflow {
									  name
				} } } } } } } } } } }`,
					{
						...context.repo,
						head: `refs/heads/${RELEASE_BRANCH}`,
						since: currentSnapshot ? currentSnapshot.tagCommit.committedDate : "1970-01-01T00:00:00Z",
					}
			)
	).repository.object.history.edges.map(edge => edge.node)

	for (let commit of commits) {
		core.info(`\nConsidering ${commit.url}`)

		if (snapshotSha && snapshotSha === commit.oid) {
			core.notice(`Snapshot is already at the newest possible commit\n${commit.url}`)
			return
		}

		for (let suite of commit.checkSuites.nodes) {
			const run = suite.workflowRun
			if (run && run.workflow.name === "CI"
					&& suite.branch && suite.branch.name === RELEASE_BRANCH
					&& suite.conclusion === "SUCCESS"
			) {
				core.notice([
					`Updating to`,
					`Commit : ${commit.url}`,
					`Run    : ${run.url}`,
				].join("\n"))
				core.setOutput("artifact_name", `${run.runNumber}-jars`);
				core.setOutput("sha", commit.oid);
				core.setOutput("workflow_run_id", run.databaseId);
				return
			}
		}
	}

	// We could look at more commits by paging the history() query but it should never be relevant so not implemented
	core.setFailed(`Nothing found from ${commits.length} newest commits`)
}
