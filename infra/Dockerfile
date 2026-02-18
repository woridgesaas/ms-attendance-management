# Build stage
FROM maven:3.9.11-eclipse-temurin-21 AS build

WORKDIR /app

# Cache dependencies first (faster builds)
COPY ../pom.xml .
RUN mvn dependency:go-offline

# Copy source
COPY ../src ./src

# Build
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY --from=build /app/target/*.jar application.jar

ENTRYPOINT ["java", "-jar", "application.jar"]