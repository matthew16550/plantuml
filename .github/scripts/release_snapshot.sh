#!/usr/bin/env bash
set -ex

cat <<-EOF >notes.txt
  This is a pre-release of the latest development work.
  ⚠️  **It is not ready for general use** ⚠️
EOF

gh release delete snapshot -y || true

git tag --force snapshot

git push --force origin snapshot

mv plantuml.jar         plantuml-SNAPSHOT.jar
mv plantuml-javadoc.jar plantuml-SNAPSHOT-javadoc.jar
mv plantuml-sources.jar plantuml-SNAPSHOT-sources.jar

gh release create --prerelease --target "${GITHUB_SHA}" --notes-file notes.txt snapshot \
  plantuml-SNAPSHOT.jar \
  plantuml-SNAPSHOT-javadoc.jar \
  plantuml-SNAPSHOT-sources.jar

echo "::notice title=::Snapshot released at ${GITHUB_SERVER_URL}/${GITHUB_REPOSITORY}/releases/tag/snapshot"
