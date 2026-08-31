# ===================================================================
# LogiTrack IQ - Multi-stage Dockerfile
# Build: Maven + JDK 17
# Runtime: OpenJDK 17 slim
# ===================================================================

# Stage 1: Build
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source and build
COPY . .
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Copy built JAR from builder stage
COPY --from=builder /app/target/demoproject-0.0.1-SNAPSHOT.jar ./demoproject.jar

# Expose port
EXPOSE 8080

# Healthcheck
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "demoproject.jar"]
