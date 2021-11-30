#!/usr/bin/env bash
set -ex

TAG="snapshot"
DATE_TIME_UTC="$(date -u +"%F at %T (UTC)")"

echo -n "${DATE_TIME_UTC}" > target/github_release_staging/plantuml-SNAPSHOT-timestamp.lock

gh release delete "${TAG}" -y || true

cat <<-EOF >notes.txt
  This is a pre-release of [the latest development work](https://github.com/plantuml/plantuml/commits/).
  ⚠️  **It is not ready for general use** ⚠️
  ⏱  _Snapshot taken the ${DATE_TIME_UTC}_
EOF

git tag --force "${TAG}"
git push --force origin "${TAG}"

gh release create --prerelease --target "${USE_SHA}" --title "${TAG}" --notes-file notes.txt "${TAG}" target/github_release_staging/*

echo "::notice title=release snapshot::Snapshot released at ${GITHUB_SERVER_URL}/${GITHUB_REPOSITORY}/releases/tag/${TAG} and taken the ${DATE_TIME_UTC}"
