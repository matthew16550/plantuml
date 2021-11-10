# This Dockerfile is used for plantuml-snapshot containers
#
# To keep the image size smaller, we do not install many fonts 

FROM azul/zulu-openjdk:11

RUN apt-get update && \
	apt-get install -y --no-install-recommends \
        graphviz \
        fonts-dejavu \
        fonts-dejavu-extra \
        fonts-liberation2 && \
    rm -rf /var/lib/apt/lists/*

COPY target/plantuml*.jar /opt/plantuml.jar

RUN mkdir /work

WORKDIR /work

RUN useradd --create-home --no-log-init --shell /bin/bash --user-group plantuml

USER plantuml

ENTRYPOINT [ "java", "-Djava.awt.headless=true", "-Duser.home=/home/plantuml", "-jar", "/opt/plantuml.jar" ]

CMD [ "-h" ]

# How to use it:
#
# docker run \
#    --cap-drop=ALL \
#    --rm \
#    --user "$(id -u):$(id -g)" \
#    --volume $(pwd):/work \
#    plantuml:snapshot -h
