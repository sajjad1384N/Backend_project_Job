# Multi-stage build for Render / any Docker host (Java 17)
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /src
COPY . .
RUN chmod +x mvnw && ./mvnw -q -DskipTests package

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /src/target/job-project-*.jar app.jar
EXPOSE 8080
# Render sets PORT at runtime; application.properties uses server.port=${PORT:8080}
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
