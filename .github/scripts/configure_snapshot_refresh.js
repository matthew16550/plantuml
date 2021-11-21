const RELEASE_BRANCH = "snapshot-release-2"

module.exports = async ({context, core, github}) => {
	core.info("Finding current snapshot ...")

	const currentSnapshot = (
			await github.graphql(`
          			query ($owner: String!, $repo: String!) {
          			  repository(owner: $owner, name: $repo) {
          				release(tagName: "snapshot") {
                            url
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
                                    message
          						  oid
          						  url
          						  checkSuites(first: 100) {
          							nodes {
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
		core.info(`Considering ${commit.url}`)

		if (snapshotSha && snapshotSha === commit.oid) {
			core.notice(`Snapshot is already at the newest possible commit ${currentSnapshot.url}`)
			return
		}

		if (commit.message.startsWith("[maven-release-plugin] prepare release ")) {
			core.info("Ignoring release commit")
			continue
		}

		for (let suite of commit.checkSuites.nodes) {
			const run = suite.workflowRun
			if (run && run.workflow.name === "CI" && suite.conclusion === "SUCCESS") {
				core.notice(`Updating to run #${run.runNumber} ${run.url}\n(commit ${commit.url})`)
				core.setOutput("sha", commit.oid);
				if (run.databaseId === context.runId && run.runNumber === context.runNumber) {
					core.setOutput("download_from_this_run", true);
				} else {
					core.setOutput("artifact_name", `${run.runNumber}-jars`);
					core.setOutput("run_id", run.databaseId);
				}
				return
			}
		}
	}

	// We could look at more commits by paging the history() query but it should never be relevant so not implemented
	core.warning(`Nothing found from ${commits.length} newest commits`)
}
