
#--------------Build-Container------------------
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app/

COPY ./ /app/

RUN mvn clean install
#--------------Execution-Container---------------
FROM eclipse-temurin:21-jdk-noble

WORKDIR /app

COPY --from=build /app/target/example-*.jar /app/example.jar

# EnableDynamicAgentLoading is necessary for mockito
CMD ["java", "-Duser.timezone=UTC", "-XX:+EnableDynamicAgentLoading", "-jar", "/app/example.jar"]
