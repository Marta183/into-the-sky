# --- Build stage: compile and package the application using Maven ---
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /into-the-sky
COPY . .
RUN mvn clean package -DskipTests

# --- Runtime stage: use JDK image for execution ---
FROM eclipse-temurin:17-jre
WORKDIR /into-the-sky
COPY --from=build /into-the-sky/discovery-server/target/discovery-server-*.jar discovery-server.jar
EXPOSE 8761
ENTRYPOINT ["java", "-jar", "discovery-server.jar"]