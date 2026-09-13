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
# The clinical events directory is the Markdown source of truth. Create it here,
# owned by the process user, so the non-root runtime can write it and so a volume
# mounted on /app/data inherits that ownership.
RUN addgroup -S careme && adduser -S careme -G careme \
    && mkdir -p /app/data/events \
    && chown -R careme:careme /app/data
COPY --from=build /workspace/target/*.jar app.jar
USER careme
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
