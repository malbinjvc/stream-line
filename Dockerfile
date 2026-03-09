# Build stage
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /build

COPY gradle gradle
COPY gradlew gradlew
COPY gradlew.bat gradlew.bat
COPY settings.gradle.kts settings.gradle.kts
COPY gradle.properties gradle.properties
COPY app/build.gradle.kts app/build.gradle.kts

RUN chmod +x gradlew && ./gradlew dependencies --no-daemon 2>/dev/null || true

COPY app/src app/src

RUN ./gradlew :app:bootJar --no-daemon -x test

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

COPY --from=builder /build/app/build/libs/stream-line.jar app.jar

RUN chown -R appuser:appgroup /app

USER appuser

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
