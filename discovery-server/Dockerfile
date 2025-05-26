FROM eclipse-temurin:17-jre
COPY target/discovery-server-*.jar discovery-server.jar
EXPOSE 8761
ENTRYPOINT ["java", "-jar", "discovery-server.jar"]