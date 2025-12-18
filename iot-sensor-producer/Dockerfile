# Build stage
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copy and install common-dto
COPY common-dto /app/common-dto
RUN mvn -f /app/common-dto/pom.xml clean install

# Copy and package iot-sensor-producer
COPY iot-sensor-producer /app/iot-sensor-producer
RUN mvn -f /app/iot-sensor-producer/pom.xml clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/iot-sensor-producer/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
