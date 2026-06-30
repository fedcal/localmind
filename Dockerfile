# ==========================================================
# LocalMind Backend - Dockerfile
# Multi-stage build: Maven + JDK 17 → JRE 17 runtime
# ==========================================================

# --- Stage 1: Build ---
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /build

# Copia POM parent e moduli per cache dipendenze
# Copy parent POM and modules for dependency caching
COPY localmind-backend/pom.xml ./pom.xml
COPY localmind-backend/localmind-shared-types/pom.xml ./localmind-shared-types/pom.xml
COPY localmind-backend/localmind-domain/pom.xml ./localmind-domain/pom.xml
COPY localmind-backend/localmind-plugin-api/pom.xml ./localmind-plugin-api/pom.xml
COPY localmind-backend/localmind-infrastructure/pom.xml ./localmind-infrastructure/pom.xml
COPY localmind-backend/localmind-api/pom.xml ./localmind-api/pom.xml
COPY localmind-backend/localmind-batch/pom.xml ./localmind-batch/pom.xml
COPY localmind-backend/localmind-app/pom.xml ./localmind-app/pom.xml

# Scarica dipendenze (cached se POM non cambiano)
# Download dependencies (cached if POMs don't change)
RUN mvn dependency:go-offline -B 2>/dev/null || true

# Copia sorgenti / Copy sources
COPY localmind-backend/ ./

# Build JAR (skip tests - i test girano in CI)
# Build JAR (skip tests - tests run in CI)
RUN mvn package -DskipTests -B \
    && cp localmind-app/target/*.jar /build/app.jar

# --- Stage 2: Runtime ---
FROM eclipse-temurin:17-jre

# Metadata
LABEL maintainer="LocalMind" \
      description="LocalMind Backend - Spring Boot" \
      version="1.0.0" \
      org.opencontainers.image.source="https://github.com/fedcal/localmind" \
      org.opencontainers.image.description="LocalMind Backend - Spring Boot" \
      org.opencontainers.image.licenses="MIT"

# Utente non-root / Non-root user
RUN groupadd -r appuser && useradd -r -g appuser -d /app appuser

WORKDIR /app

# Copia JAR dal builder / Copy JAR from builder
COPY --from=builder /build/app.jar app.jar

# Directory upload documenti / Document upload directory
RUN mkdir -p /data/uploads && chown -R appuser:appuser /data/uploads /app

USER appuser

EXPOSE 8080

# Health check
HEALTHCHECK --interval=15s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -sf http://localhost:8080/api/v1/dashboard/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
