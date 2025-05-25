# --- Build stage: compile and package the application using Maven ---
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /into-the-sky
COPY . .
RUN mvn clean package -DskipTests

# --- Runtime stage: lightweight JRE for production ---
FROM eclipse-temurin:17-jre
WORKDIR /into-the-sky
COPY --from=build /into-the-sky/gateway/target/gateway-*.jar gateway.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "gateway.jar"]