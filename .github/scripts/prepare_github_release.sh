#! /usr/bin/env bash
set -ex
tree target

nexus_dir="../nexus-staging/deferred/net/sourceforge/plantuml/plantuml/${STAGING_VERSION}"
release_dir="target/github_release"

mkdir "${release_dir}"

ln -s "${nexus_dir}/plantuml-${STAGING_VERSION}.pom"             "${release_dir}/plantuml-${RELEASE_VERSION}.pom"
ln -s "${nexus_dir}/plantuml-${STAGING_VERSION}.pom.asc"         "${release_dir}/plantuml-${RELEASE_VERSION}.pom.asc"
ln -s "${nexus_dir}/plantuml-${STAGING_VERSION}.jar"             "${release_dir}/plantuml-${RELEASE_VERSION}.jar"
ln -s "${nexus_dir}/plantuml-${STAGING_VERSION}.jar.asc"         "${release_dir}/plantuml-${RELEASE_VERSION}.jar.asc"
ln -s "${nexus_dir}/plantuml-${STAGING_VERSION}-javadoc.jar"     "${release_dir}/plantuml-${RELEASE_VERSION}-javadoc.jar"
ln -s "${nexus_dir}/plantuml-${STAGING_VERSION}-javadoc.jar.asc" "${release_dir}/plantuml-${RELEASE_VERSION}-javadoc.jar.asc"
ln -s "${nexus_dir}/plantuml-${STAGING_VERSION}-sources.jar"     "${release_dir}/plantuml-${RELEASE_VERSION}-sources.jar"
ln -s "${nexus_dir}/plantuml-${STAGING_VERSION}-sources.jar.asc" "${release_dir}/plantuml-${RELEASE_VERSION}-sources.jar.asc"

tree target
