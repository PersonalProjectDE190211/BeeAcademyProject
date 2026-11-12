# ==============================================================================
# Multi-Stage Dockerfile at Repository Root for Render Deployment
# Hỗ trợ trường hợp Render để trống Root Directory (mặc định ở gốc repo)
# ==============================================================================

# Stage 1: Build JAR using Maven Wrapper
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /workspace

# Copy Maven wrapper và pom.xml từ thư mục backend/
COPY backend/.mvn/ .mvn/
COPY backend/mvnw backend/pom.xml ./
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copy mã nguồn backend và đóng gói
COPY backend/src/ src/
RUN ./mvnw clean package -DskipTests -B

# Stage 2: Minimal Runtime JRE Image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup
RUN apk --no-cache add curl

COPY --from=builder --chown=appuser:appgroup /workspace/target/*.jar app.jar

USER appuser

EXPOSE 8080

ENV JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:${PORT:-8080}/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dserver.port=${PORT:-8080} -jar app.jar"]

