FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /workspace

COPY pom.xml pom.xml
COPY de.terministic.fabsimcore/pom.xml de.terministic.fabsimcore/pom.xml
COPY de.terministic.fabsimcore/src de.terministic.fabsimcore/src
COPY de.terministic.fabsimmetamodel/pom.xml de.terministic.fabsimmetamodel/pom.xml
COPY de.terministic.fabsimmetamodel/src de.terministic.fabsimmetamodel/src

RUN mvn -Dmaven.test.skip=true package

FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=build /workspace/de.terministic.fabsimmetamodel/target/fabsim.jar /app/fabsim.jar

ENTRYPOINT ["java", "-jar", "/app/fabsim.jar"]
