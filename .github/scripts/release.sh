#!/usr/bin/env bash
set -ex

TAG="v${RELEASE_VERSION}"

cp -f "plantuml-${RELEASE_VERSION}.pom" pom.xml
git add pom.xml
git commit -m "Release ${RELEASE_VERSION}"

git tag "${TAG}"

git push origin "${TAG}"

gh release create --title "${TAG}" "${TAG}" \
  "plantuml-${RELEASE_VERSION}.pom" \
  "plantuml-${RELEASE_VERSION}.pom.asc" \
  "plantuml-${RELEASE_VERSION}.jar" \
  "plantuml-${RELEASE_VERSION}.jar.asc" \
  "plantuml-${RELEASE_VERSION}-javadoc.jar" \
  "plantuml-${RELEASE_VERSION}-javadoc.jar.asc" \
  "plantuml-${RELEASE_VERSION}-sources.jar" \
  "plantuml-${RELEASE_VERSION}-sources.jar.asc" \
  "plantuml-${RELEASE_VERSION}-timestamp.lock"

echo "::notice title=::Released at ${GITHUB_SERVER_URL}/${GITHUB_REPOSITORY}/releases/tag/${TAG} 🎉"
