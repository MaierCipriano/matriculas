FROM maven:3.8-eclipse-temurin-11 AS build
WORKDIR /app
COPY . .
RUN mvn -q clean package -DskipTests

FROM eclipse-temurin:11-jre
WORKDIR /app
COPY --from=build /app/target/mail-service-0.0.1-SNAPSHOT.jar app.jar
ARG APP_PORT=9102
ENV SERVER_PORT=${APP_PORT}
EXPOSE ${APP_PORT}
CMD ["java", "-Dspring.profiles.active=docker", "-Dserver.port=${SERVER_PORT}", "-jar", "app.jar"]