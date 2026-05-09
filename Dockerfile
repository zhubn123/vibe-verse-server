FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /workspace

COPY pom.xml .
RUN mvn -B -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -B -DskipTests clean package \
    && JAR_PATH="$(ls target/*.jar | grep -v '\.original$' | grep -v 'plain' | head -n 1)" \
    && cp "${JAR_PATH}" /workspace/app.jar

FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS=""

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

COPY --from=build /workspace/app.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /app/app.jar"]
