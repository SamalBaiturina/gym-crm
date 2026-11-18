# Stage 1: build the jar with Maven
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src ./src
RUN mvn -B package -DskipTests -Djacoco.skip=true

# Stage 2: run it on a small JRE image
FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system --uid 1001 --no-create-home spring
COPY --from=build /build/target/*.jar app.jar
USER spring
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
