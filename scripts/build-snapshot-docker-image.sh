#! /usr/bin/env bash

set -e

if [[ -z "${IMAGE_REPO}" ]]; then
	IMAGE_REPO="plantuml-snapshot"
fi

if [[ -z "${IMAGE_REVISION}" ]]; then
	IMAGE_REVISION="$(git rev-parse HEAD)"
fi

IMAGE_VERSION="$(mvn -q -Dexec.executable=echo -Dexec.args='${project.version}' --non-recursive exec:exec)"

TAG_LATEST="${IMAGE_REPO}:latest"
TAG_REVISION="${IMAGE_REPO}:${IMAGE_REVISION}"
TAG_VERSION="${IMAGE_REPO}:${IMAGE_VERSION}"

docker build \
	--file "snapshot.Dockerfile" \
	--label "org.opencontainers.image.description=SNAPSHOT release of PlantUml.  Beware this is bleeding edge, sometimes it will be broken!" \
	--label "org.opencontainers.image.created=$(TZ=utc date +%Y-%m-%dT%H-%M-%SZ)" \
	--label "org.opencontainers.image.revision=${IMAGE_REVISION}" \
	--label "org.opencontainers.image.source=${IMAGE_SOURCE}" \
	--label "org.opencontainers.image.version=${IMAGE_VERSION}" \
	--tag "${TAG_LATEST}" \
	--tag "${TAG_REVISION}" \
	--tag "${TAG_VERSION}" \
	.

echo "::set-output name=tag_latest::${TAG_LATEST}"
echo "::set-output name=tag_revision::${TAG_REVISION}"
echo "::set-output name=tag_version::${TAG_VERSION}"
