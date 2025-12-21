#! /usr/bin/env bash

# This script creates a new repo in https://codeberg.org ready for use as a mirror from GitHub.
#
# The mirroring is done by .github/workflows/mirror.yml

OWNER="plantuml"

if [ -z "${CODEBERG_TOKEN}" ]; then
  cat >&2 <<EOF
CODEBERG_TOKEN env var is not set

Make a token at https://codeberg.org/user/settings/applications with these options:
- Public only
- Select Permissions
  - repository = Read and Write
  - user = Read and Write
  - all other permission = No access

*** You should probably delete the token in the Codeberg UI after finishing here ***
EOF
  exit 1
fi

if [ "$#" -ne 2 ]; then
  echo "USAGE: $(basename $0) <repo_name> <default_branch>" >&2
  exit 1
fi

set -euo pipefail

repo_name="$1"
default_branch="$2"

# API reference: https://codeberg.org/api/swagger#/repository/createCurrentUserRepo
curl -X POST \
  -H "Authorization: token ${CODEBERG_TOKEN}" \
  -H "Accept: application/json" \
  -H "Content-Type: application/json" \
  -d @- \
  https://codeberg.org/api/v1/user/repos <<EOF
{
  "name":               "${repo_name}",
  "default_branch":     "${default_branch}",
  "description":        "READ-ONLY code mirror from https://github.com/${OWNER}/${repo_name}"
}
EOF

# API reference: https://codeberg.org/api/swagger#/repository/repoEdit
curl -X PATCH \
  -H "Authorization: token ${CODEBERG_TOKEN}" \
  -H "Accept: application/json" \
  -H "Content-Type: application/json" \
  -d @- \
  "https://codeberg.org/api/v1/repos/${OWNER}/${repo_name}" <<EOF
{
  "has_issues":           false,
  "has_pull_requests":    false
}
EOF
