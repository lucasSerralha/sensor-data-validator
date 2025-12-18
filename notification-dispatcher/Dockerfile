# Build stage
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copy and install common-dto
COPY common-dto /app/common-dto
RUN mvn -f /app/common-dto/pom.xml clean install

# Copy and package notification-dispatcher
COPY notification-dispatcher /app/notification-dispatcher
RUN mvn -f /app/notification-dispatcher/pom.xml clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/notification-dispatcher/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
