#! /usr/bin/env bash
set -ex
tree target

from_dir="target/nexus-staging/deferred/net/sourceforge/plantuml/plantuml/${FROM_VERSION}"
to_dir="target/github_release_staging"

mkdir "${to_dir}"

ln -s "${from_dir}/plantuml-${FROM_VERSION}.pom"             "${to_dir}/plantuml-${TO_VERSION}.pom"
ln -s "${from_dir}/plantuml-${FROM_VERSION}.pom.asc"         "${to_dir}/plantuml-${TO_VERSION}.pom.asc"
ln -s "${from_dir}/plantuml-${FROM_VERSION}.jar"             "${to_dir}/plantuml-${TO_VERSION}.jar"
ln -s "${from_dir}/plantuml-${FROM_VERSION}.jar.asc"         "${to_dir}/plantuml-${TO_VERSION}.jar.asc"
ln -s "${from_dir}/plantuml-${FROM_VERSION}-javadoc.jar"     "${to_dir}/plantuml-${TO_VERSION}-javadoc.jar"
ln -s "${from_dir}/plantuml-${FROM_VERSION}-javadoc.jar.asc" "${to_dir}/plantuml-${TO_VERSION}-javadoc.jar.asc"
ln -s "${from_dir}/plantuml-${FROM_VERSION}-sources.jar"     "${to_dir}/plantuml-${TO_VERSION}-sources.jar"
ln -s "${from_dir}/plantuml-${FROM_VERSION}-sources.jar.asc" "${to_dir}/plantuml-${TO_VERSION}-sources.jar.asc"

tree target
