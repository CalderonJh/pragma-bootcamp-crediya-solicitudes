# Build stage
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY  applications/app-service/build/libs/app.jar app.jar

# Optional: extract layers
RUN java -Djarmode=layertools -jar app.jar extract || true

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
WORKDIR /app
COPY --from=builder /app/app.jar app.jar

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=70 -Djava.security.egd=file:/dev/./urandom"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]