# Stage 1: Build the app with Maven
FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /build
COPY . .
RUN mvn clean package -DskipTests

# Stage 2: Run the built JAR
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY --from=builder /build/target/*.jar app.jar

# Expose Spring Boot's port
EXPOSE 9001

# Run the Spring Boot app
ENTRYPOINT ["java", "-jar", "app.jar"]
