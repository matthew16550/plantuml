#!/usr/bin/env bash
set -ex

TAG="snapshot"
DATE_TIME_UTC=$(date -u +"%F at %T (UTC)")

gh release delete "${TAG}" -y || true

git tag --force "${TAG}"

git push --force origin "${TAG}"

mv plantuml-0-SNAPSHOT.pom             plantuml-SNAPSHOT.pom
mv plantuml-0-SNAPSHOT.pom.asc         plantuml-SNAPSHOT.pom.asc
mv plantuml-0-SNAPSHOT.jar             plantuml-SNAPSHOT.jar
mv plantuml-0-SNAPSHOT.jar.asc         plantuml-SNAPSHOT.jar.asc
mv plantuml-0-SNAPSHOT-javadoc.jar     plantuml-SNAPSHOT-javadoc.jar
mv plantuml-0-SNAPSHOT-javadoc.jar.asc plantuml-SNAPSHOT-javadoc.jar.asc
mv plantuml-0-SNAPSHOT-sources.jar     plantuml-SNAPSHOT-sources.jar
mv plantuml-0-SNAPSHOT-sources.jar.asc plantuml-SNAPSHOT-sources.jar.asc

echo -n "${DATE_TIME_UTC}" > plantuml-SNAPSHOT-timestamp.lock

cat <<-EOF >notes.txt
  This is a pre-release of [the latest development work](https://github.com/plantuml/plantuml/commits/).
  ⚠️  **It is not ready for general use** ⚠️
  ⏱  _Snapshot taken the ${DATE_TIME_UTC}_
EOF

gh release create --prerelease --target "${USE_SHA}" --title "${TAG}" --notes-file notes.txt "${TAG}" \
  plantuml-SNAPSHOT.pom \
  plantuml-SNAPSHOT.pom.asc \
  plantuml-SNAPSHOT.jar \
  plantuml-SNAPSHOT.jar.asc \
  plantuml-SNAPSHOT-javadoc.jar \
  plantuml-SNAPSHOT-javadoc.jar.asc \
  plantuml-SNAPSHOT-sources.jar \
  plantuml-SNAPSHOT-sources.jar.asc \
  plantuml-SNAPSHOT-timestamp.lock

echo "::notice title=release snapshot::Snapshot released at ${GITHUB_SERVER_URL}/${GITHUB_REPOSITORY}/releases/tag/${TAG} and taken the ${DATE_TIME_UTC}"
