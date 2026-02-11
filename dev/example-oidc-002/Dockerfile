
#--------------Build-Container------------------
FROM maven:3.9-eclipse-temurin-21  AS build

WORKDIR /app/

COPY ./ /app/

RUN mvn clean install
#--------------Execution-Container---------------
FROM eclipse-temurin:21-jdk-noble

WORKDIR /app

COPY --from=build /app/target/example-*.jar /app/example.jar

CMD ["java", "-Duser.timezone=UTC", "-jar", "/app/example.jar"]
