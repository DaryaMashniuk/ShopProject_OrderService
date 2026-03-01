FROM maven:3.9.11-eclipse-temurin-21 AS builder
WORKDIR /opt/app
COPY mvnw pom.xml ./
COPY ./src ./src
RUN mvn clean install -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /opt/app
EXPOSE 8080
COPY --from=builder /opt/app/target/*.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]