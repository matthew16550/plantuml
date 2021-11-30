const MAIN_BRANCH = "maven-release-from-github"

const AUTHORIZED_RELEASERS = new Set([
	"arnaudroques",
	context.repo.owner, // kludge so that forked repos can release themselves when testing the workflow
])

const SHA_REGEX = /^[0-9a-fA-F]{40}$/

const VERSION_REGEX = /^1\.20[0-9][0-9]\.[0-9]+$/

module.exports = ({context, core, github}) => {
	core.info(`context.eventName  '${context.eventName}'`)
	core.info(`context.actor      '${context.actor}'`)
	core.info(`context.ref        '${context.ref}'`)
	core.info(`context.repo.owner '${context.repo.owner}'`)
	// todo print the inputs?

	const workflow_dispatch = context.eventName === "workflow_dispatch"
	const release_version = workflow_dispatch ? context.payload.inputs.release_version : null
	const use_sha = workflow_dispatch ? context.payload.inputs.use_sha : null

	if (release_version && !release_version.match(VERSION_REGEX)) {
		core.setFailed(`release_version '${release_version}' does not match regex /${VERSION_REGEX}/`)
		return
	}

	if (use_sha && !use_sha.match(SHA_REGEX)) {
		core.setFailed(`use_sha '${use_sha}' does not match regex /${SHA_REGEX}/`)
		return
	}

	// Which commit to use
	core.setOutput("use_sha", use_sha ? use_sha : github.sha)

	// Release a new version
	if (workflow_dispatch && context.payload.inputs.release_version) {
		if (!AUTHORIZED_RELEASERS.has(context.actor)) {
			core.setFailed(`Sorry, '${context.actor}' is not authorized to make a release`)
			return
		}

		if (context.ref !== `refs/heads/${MAIN_BRANCH}`) {
			core.setFailed(`Releases can only be made from the '${MAIN_BRANCH}' branch`)
			return
		}

		core.notice(`This run will release '${release_version}'`)
		core.setOutput("do_release", true)
		core.setOutput("release_version", release_version)
		return
	}

	// Update the snapshot release
	if (context.ref === `refs/heads/${MAIN_BRANCH}`) {
		core.notice("This run will update the snapshot release")
		core.setOutput("do_snapshot_release", true)
	}
}
