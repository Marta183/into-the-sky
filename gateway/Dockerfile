# --- Build stage: compile and package the application using Maven ---
FROM maven:3.8.8-openjdk-18 AS build
#WORKDIR /app
COPY pom.xml ./
COPY gateway/pom.xml ./
COPY gateway/src ./src
RUN mvn clean package -DskipTests

# --- Runtime stage: lightweight JRE for production ---
FROM eclipse-temurin:18-jre-alpine
#WORKDIR /app
COPY --from=build /target/gateway-api.jar gateway-api.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "gateway-api.jar"]