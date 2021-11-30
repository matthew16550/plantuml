#!/usr/bin/env bash
set -ex

TAG="v${RELEASE_VERSION}"

cp -f "target/github_release/plantuml-${RELEASE_VERSION}.pom" pom.xml
git add pom.xml
git commit -m "Release ${RELEASE_VERSION}"

# todo make some notes!
cat <<-EOF >notes.txt
EOF

git tag "${TAG}"

git push origin "${TAG}"

gh release create \
  --repo "${GITHUB_REPOSITORY}" \
  --title "${TAG}" \
  --notes-file notes.txt \
  "${TAG}" target/github_release/*

echo "::notice title=::Released at ${GITHUB_SERVER_URL}/${GITHUB_REPOSITORY}/releases/tag/${TAG} 🎉"
