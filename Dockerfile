FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /workspace

COPY de.terministic.fabsimcore/pom.xml de.terministic.fabsimcore/pom.xml
COPY de.terministic.fabsimcore/src de.terministic.fabsimcore/src
COPY de.terministic.fabsimmetamodel/pom.xml de.terministic.fabsimmetamodel/pom.xml
COPY de.terministic.fabsimmetamodel/src de.terministic.fabsimmetamodel/src

RUN mvn -f de.terministic.fabsimcore/pom.xml -Dmaven.test.skip=true install \
	&& mvn -f de.terministic.fabsimmetamodel/pom.xml -Dmaven.test.skip=true package \
	&& mkdir -p /workspace/runtime-libs \
	&& find /root/.m2/repository -name '*.jar' \
		! -name 'slf4j-api-1.5.6.jar' \
		! -name 'slf4j-jdk14-1.5.6.jar' \
		-exec cp {} /workspace/runtime-libs/ \;

FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=build /workspace/de.terministic.fabsimmetamodel/target/metamodel-0.0.1-SNAPSHOT.jar /app/minifab.jar
COPY --from=build /workspace/runtime-libs /app/lib

RUN printf '%s\n' '#!/bin/sh' \
	'exec java -cp "/app/minifab.jar:/app/lib/*" de.terministic.fabsim.metamodel.examples.MiniFabDockerApp "$@"' \
	> /app/entrypoint.sh \
	&& chmod +x /app/entrypoint.sh

ENTRYPOINT ["/app/entrypoint.sh"]
