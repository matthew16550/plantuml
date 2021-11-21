#! /usr/bin/env bash
set -ex

cat <<-EOF >release-notes.txt
EOF

mv plantuml.jar         "plantuml-${RELEASE_VERSION}.jar"
mv plantuml-javadoc.jar "plantuml-${RELEASE_VERSION}-javadoc.jar"
mv plantuml-sources.jar "plantuml-${RELEASE_VERSION}-sources.jar"

gh release create --target "${RELEASE_SHA}" --title "${TAG}" --notes-file release-notes.txt "${TAG}" \
  "plantuml-${RELEASE_VERSION}.jar" \
  "plantuml-${RELEASE_VERSION}-javadoc.jar" \
  "plantuml-${RELEASE_VERSION}-sources.jar"

echo "::notice title=::Released ${GITHUB_SERVER_URL}/${GITHUB_REPOSITORY}/releases/tag/${TAG} 🎉"
