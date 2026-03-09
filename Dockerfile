#build
FROM maven:4.0.0-rc-5-amazoncorretto-17-al2023 AS builder
WORKDIR /itmo-nosql-esaraeva

COPY /pom.xml ./
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B clean -DskipTests dependency:go-offline

COPY /src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B clean package -DskipTests

FROM amazoncorretto:17-al2023-headless
WORKDIR /itmo-nosql-esaraeva

COPY --from=builder /itmo-nosql-esaraeva/target/*.jar app.jar
EXPOSE ${APP_PORT}
ENTRYPOINT ["java", "-jar", "app.jar"]