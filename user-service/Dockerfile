# --- Build stage: compile and package the application using Maven ---
FROM maven:3.8.8-openjdk-18 AS build
#WORKDIR /app
COPY pom.xml ./
COPY user-service/pom.xml ./
COPY user-service/src ./src
RUN mvn clean package -DskipTests

# --- Runtime stage: lightweight JRE for production ---
FROM eclipse-temurin:18-jre-alpine
#WORKDIR /app
COPY --from=build /target/user-service-api.jar user-service-api.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "user-service-api.jar"]