#! /usr/bin/env bash
set -ex
env

TAG=snapshot

url="https://maven.pkg.github.com/${REPO_OWNER}/plantuml/net/sourceforge/plantuml/plantuml/${RELEASE_SHA}/plantuml-${RELEASE_SHA}"

# auth is read from the .netrc file
wget -q "${url}.jar"         -O plantuml-SNAPSHOT.jar 
wget -q "${url}-javadoc.jar" -O plantuml-SNAPSHOT-javadoc.jar 
wget -q "${url}-sources.jar" -O plantuml-SNAPSHOT-sources.jar 

gh release delete "${TAG}" || true
git push --delete origin "${TAG}" || true
git tag --delete "${TAG}" || true

cat <<-EOF >release-notes.txt
  This is a pre-release of the latest development work.
  ⚠️  **It is not ready for general use** ⚠️
  ⏱  _Snapshot taken $(date -u +"%F at %T (UTC)")_
EOF

gh release create --prerelease --target "${RELEASE_SHA}" --title "${TAG}" --notes-file release-notes.txt "${TAG}" \
  plantuml-SNAPSHOT.jar \
  plantuml-SNAPSHOT-javadoc.jar \
  plantuml-SNAPSHOT-sources.jar

echo "::notice title=::Snapshot released at ${GITHUB_SERVER_URL}/${GITHUB_REPOSITORY}/releases/tag/${TAG}"
