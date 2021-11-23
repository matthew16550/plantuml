const RELEASE_BRANCH = "snapshot-release-2"

module.exports = async ({context, core, github}) => {
	core.info("Finding current snapshot ...")

	const currentSnapshot = await findCurrentSnapshot(context, github);

	core.info(`Current snapshot: ${currentSnapshot || "NONE"}`)

	core.info(`Finding newest commits ...`)

	const commits = await findNewestCommits(`refs/heads/${RELEASE_BRANCH}`, context, github);

	for (let commit of commits) {
		const sha = commit.oid;
		core.info(`Considering ${commit.url}`)

		if (currentSnapshot && currentSnapshot === sha) {
			core.notice(`Snapshot is already at the newest possible commit ${commit.url}`)
			return
		}

		if (await packageVersionExists("net.sourceforge.plantuml.plantuml", sha, context, github)) {
			core.notice(`Updating to ${commit.url})`)
			core.setOutput("new_sha", sha);
			return
		}
	}

	// We could look at more commits by paging the history() query but usually it should not be relevant so not implemented yet
	core.warning(`Nothing found from ${commits.length} newest commits`)
}

async function findCurrentSnapshot(context, github) {
	try {
		const response = await github.rest.repos.getReleaseByTag({
			...context.repo,
			tag: "snapshot",
		})
		return response.target_commitish
	} catch (error) {
		if (error.status === 404)
			return null;
		throw error
	}
}

async function findNewestCommits(head, context, github) {
	return (
			await github.graphql(`
          			query($owner: String!, $repo: String!, $head: String!) {
          			  repository(owner: $owner, name: $repo) {
          				object(expression: $head) {
          				  ... on Commit {
          					history(first: 100) {
							  nodes {
								oid
								url
          			} } } } } }`,
					{...context.repo, head}
			)
	).repository.object.history.nodes;
}

async function packageVersionExists(name, version, context, github) {
	const packages = (
			await github.graphql(`
					query($owner: String!, $repo: String!, $name: String!, $version: String!) {
					  repository(owner: $owner, name: $repo) {
						packages(first: 1, names: [$name]) {
						  nodes {
							version(version: $version) {
							  id
					} } } } }`,
					{...context.repo, name, version}
			)
	).repository.packages.nodes

	return packages.length && packages[0].version
}
