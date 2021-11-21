#! /usr/bin/env bash
set -e

POM_VERSION="$(mvn -q -Dexec.executable=echo -Dexec.args='${project.version}' exec:exec)"

cat <<-EOF
  GITHUB_EVENT_NAME       : '${GITHUB_EVENT_NAME}'
  GITHUB_REF              : '${GITHUB_REF}'
  GITHUB_ACTOR            : '${GITHUB_ACTOR}'
  GITHUB_REPOSITORY_OWNER : '${GITHUB_REPOSITORY_OWNER}'
  POM_VERSION             : '${POM_VERSION}'
EOF

if [[ "${GITHUB_EVENT_NAME}" != "push" ]]; then
  echo "No release (not a push)"
  exit 0
fi

if [[ "${GITHUB_REF}" != "refs/tags/v${POM_VERSION}" ]]; then
  echo "No release (not a version tag)"
  exit 0
fi

# We match against github.repository_owner as a kludge so that forked repos can release themselves when testing the workflow
if [[ "${GITHUB_ACTOR}" == "arnaudroques" || "${ACTOR}" == "${GITHUB_REPOSITORY_OWNER}" ]]; then
  echo "No release (wrong actor)"
  exit 0
fi

echo "::notice title=::This run will release '${POM_VERSION}'"
echo "::set-output name=release_version::${POM_VERSION}"
