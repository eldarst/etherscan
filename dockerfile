FROM openjdk:21-slim AS builder
WORKDIR /app

COPY gradlew .
COPY gradle gradle

COPY . .

RUN chmod +x gradlew && ./gradlew clean bootJar --no-daemon

FROM openjdk:21-slim
WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
