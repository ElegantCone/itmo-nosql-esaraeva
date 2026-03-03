#build
FROM maven:4.0.0-rc-5-amazoncorretto-17-al2023 as builder
WORKDIR /itmo-nosql-esaraeva
COPY . .
RUN mvn clean package spring-boot:repackage -Dmaven.test.skip=true

#run
FROM maven:4.0.0-rc-5-amazoncorretto-17-al2023
WORKDIR /app
COPY --from=builder /itmo-nosql-esaraeva/target/*.jar app.jar
EXPOSE ${APP_PORT}
ENTRYPOINT ["java", "-jar", "app.jar"]