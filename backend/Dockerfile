# ==============================================================================
# Multi-Stage Dockerfile for BeeAcademy Spring Boot Backend (Java 21)
# ==============================================================================

# Stage 1: Build JAR using Maven Wrapper
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /workspace

# Tận dụng Docker layer caching cho dependencies
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copy mã nguồn và đóng gói
COPY src/ src/
RUN ./mvnw clean package -DskipTests -B

# Stage 2: Minimal Runtime JRE Image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Tạo non-root user để tăng cường bảo mật container
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Cài đặt curl/wget cho Healthcheck probe
RUN apk --no-cache add curl

# Copy file jar từ builder
COPY --from=builder --chown=appuser:appgroup /workspace/target/*.jar app.jar

USER appuser

EXPOSE 8080

# Cấu hình GC và bộ nhớ tối ưu cho môi trường container
ENV JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:${PORT:-8080}/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dserver.port=${PORT:-8080} -jar app.jar"]

