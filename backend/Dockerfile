# Build stage
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S careme && adduser -S careme -G careme
COPY --from=build /workspace/target/*.jar app.jar
USER careme
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
