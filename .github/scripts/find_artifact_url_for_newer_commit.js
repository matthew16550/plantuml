const {spawnSync} = require("child_process")

module.exports = async ({context, core, github}) => {
	core.info("Finding current snapshot ...")
	const snapshot = await find_current_snapshot_sha(context, github)

	core.info(`Current snapshot: ${snapshot}`)

	core.info("Finding newer commits ...")
	const newer_commits = find_newer_commit_shas(snapshot)

	if (!newer_commits) {
		core.info("Snapshot already at newest commit")
		return null
	}

	core.info(`Newer commits:\n${newer_commits.join("\n")}`)

	for (let sha of newer_commits) {
		core.info(`Finding data for ${sha} ...`)
		const {state, workflowRuns} = await find_commit_data(sha, context, github)

		if (!state) {
			core.error(`Commit ${sha} not found`)
			continue
		}

		if (state !== "SUCCESS") {
			core.info(`Ignoring ${state} commit ${sha}`)
			continue
		}

		for (let run of workflowRuns) {
			core.info(`Finding artifact from workflow run ${run.url} ...`)
			const url = await find_artifact_url_from_workflow_run(run.databaseId, context, github)
			
			if (url) {
				core.info(`Using ${url} for ${sha}`)
				return url
			}
		}
		
		console.info(`Ignoring ${sha} because no suitable artifact found`)
	}

	core.info(`No newer artifact found`)
	return null
}

async function find_current_snapshot_sha(context, github) {
	return await github.rest.repos.getReleaseByTag({
		...context.repo,
		tag: "snapshot",
	}).target_commitish
}

function find_newer_commit_shas(snapshot) {
	const result = spawnSync("git", ["rev-list", `${snapshot}..HEAD`])  // result is ordered newest first
	if (result.error)
		throw result.error
	return result.stdout.split(/\n/) 	// TODO empty list ?
}

async function find_commit_data(sha, context, github) {
	const response = await github.graphql(`
				query($owner:String!, $name:String!, sha:String!) {
				  repository(owner:$owner, name:$name) {
					object(oid: sha) {
					  ... on Commit {
						statusCheckRollup {
						  state
						}
						checkSuites(first:100) {
						  nodes {
							workflowRun {
							  databaseId
							  url
							}
						  }
						}
					  }
					}
				  }
				}`,
			{
				owner: context.repo.owner,
				name: context.repo.repo,
				sha,
			}) // TODO errors throw ?

	const object = response.data.repository.object;
	return {
		state: object ? object.statusCheckRollup.state : null,
		workflowRuns: object ? object.checkSuites.nodes.flatMap(node => node.workflowRun).filter(!!run) : []
	}
}

async function find_artifact_url_from_workflow_run(runId, context, github) {
	const response = await github.rest.actions.listWorkflowRunArtifacts({
		...context.repo,
		run_id: runId,
	});// TODO error throws?
	let artifact = response.artifacts.find(a => a.name.endsWith("-jars"));
	return artifact ? artifact.archive_download_url : null
}

// https://api.github.com/repos/matthew16550/plantuml/actions/runs/1458211290/artifacts

// https://api.github.com/repos/plantuml/plantuml/releases/tags/snapshot

// https://api.github.com/repos/plantuml/plantuml/check-suites/4389169301
