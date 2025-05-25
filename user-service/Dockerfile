# --- Build stage: compile and package the application using Maven ---
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /into-the-sky
COPY . .
RUN mvn clean package -DskipTests

# --- Runtime stage: lightweight JRE for production ---
FROM eclipse-temurin:17-jre
WORKDIR /into-the-sky
COPY --from=build /into-the-sky/user-service/target/user-service-*.jar user-service.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "user-service.jar"]