# Multi-stage Dockerfile for NexusMarket Microservice
# Stage 1: Build executable Spring Boot fat JAR
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /workspace

# Copy Maven Wrapper and POM, then resolve dependencies
COPY .mvn ./.mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B || true

# Copy source code and package executable Spring Boot JAR (repackage goal)
COPY src ./src
RUN ./mvnw clean package -DskipTests -B

# Stage 2: Hardened Runtime Image
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Non-root user for security
RUN addgroup --system spring && adduser --system --ingroup spring spring
USER spring:spring

# Copy the repackaged executable Spring Boot JAR (excluding .jar.original)
COPY --from=builder /workspace/target/nexusmarket-domain-1.0.0.jar app.jar

ENV SERVER_PORT=8080 \
    JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
