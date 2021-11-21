#! /usr/bin/env bash
set -ex

TAG=snapshot

gh release delete "${TAG}" || true
git push --delete origin "${TAG}" || true
git tag --delete "${TAG}" || true

cat <<-EOF >release-notes.txt
  This is a pre-release of the latest development work.
  ⚠️  **It is not ready for general use** ⚠️
  ⏱  _Snapshot taken $(date -u +"%F at %T (UTC)")_
EOF

mv plantuml.jar         plantuml-SNAPSHOT.jar
mv plantuml-javadoc.jar plantuml-SNAPSHOT-javadoc.jar
mv plantuml-sources.jar plantuml-SNAPSHOT-sources.jar

gh release create --prerelease --target "${RELEASE_SHA}" --title "${TAG}" --notes-file release-notes.txt "${TAG}" \
  plantuml-SNAPSHOT.jar \
  plantuml-SNAPSHOT-javadoc.jar \
  plantuml-SNAPSHOT-sources.jar

echo "::notice title=::Snapshot released at ${GITHUB_SERVER_URL}/${GITHUB_REPOSITORY}/releases/tag/${TAG}"
