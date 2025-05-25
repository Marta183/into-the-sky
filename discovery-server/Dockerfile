# --- Build stage: compile and package the application using Maven ---
FROM maven:3.8.8-openjdk-18 AS build
#WORKDIR /app
COPY pom.xml ./
COPY discovery-server/pom.xml ./
COPY discovery-server/src ./src
RUN mvn clean package -DskipTests

# --- Runtime stage: use JDK image for execution ---
FROM eclipse-temurin:18-jre-alpine
#WORKDIR /app
COPY --from=build /target/discovery-server-api.jar discovery-server-api.jar
EXPOSE 8761
ENTRYPOINT ["java", "-jar", "discovery-server-api.jar"]
