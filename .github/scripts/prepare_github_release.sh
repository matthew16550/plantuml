#! /usr/bin/env bash
set -ex
tree target

from_dir="target/nexus-staging/deferred/net/sourceforge/plantuml/plantuml/${FROM_VERSION}"
to_dir="target/github_release_staging"

mkdir "${to_dir}"

# todo ln
cp -v "${from_dir}/plantuml-${FROM_VERSION}.pom"             "${to_dir}/plantuml-${TO_VERSION}.pom"
cp -v "${from_dir}/plantuml-${FROM_VERSION}.pom.asc"         "${to_dir}/plantuml-${TO_VERSION}.pom.asc"
cp -v "${from_dir}/plantuml-${FROM_VERSION}.jar"             "${to_dir}/plantuml-${TO_VERSION}.jar"
cp -v "${from_dir}/plantuml-${FROM_VERSION}.jar.asc"         "${to_dir}/plantuml-${TO_VERSION}.jar.asc"
cp -v "${from_dir}/plantuml-${FROM_VERSION}-javadoc.jar"     "${to_dir}/plantuml-${TO_VERSION}-javadoc.jar"
cp -v "${from_dir}/plantuml-${FROM_VERSION}-javadoc.jar.asc" "${to_dir}/plantuml-${TO_VERSION}-javadoc.jar.asc"
cp -v "${from_dir}/plantuml-${FROM_VERSION}-sources.jar"     "${to_dir}/plantuml-${TO_VERSION}-sources.jar"
cp -v "${from_dir}/plantuml-${FROM_VERSION}-sources.jar.asc" "${to_dir}/plantuml-${TO_VERSION}-sources.jar.asc"
tree target
