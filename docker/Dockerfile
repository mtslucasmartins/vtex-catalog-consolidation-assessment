# Shared build for either service. Build context is the repository root.
ARG SERVICE

FROM maven:3.9-eclipse-temurin-21 AS build
ARG SERVICE
WORKDIR /workspace
COPY ${SERVICE}/pom.xml ./pom.xml
RUN mvn -q -B dependency:go-offline
COPY ${SERVICE}/src ./src
RUN mvn -q -B -DskipTests package && cp target/*.jar app.jar

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system --uid 1001 app && mkdir -p /app/data && chown -R app:app /app
COPY --from=build --chown=app:app /workspace/app.jar /app/app.jar
USER app
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75"
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
