FROM eclipse-temurin:17-jre
COPY target/gateway-*.jar gateway.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "gateway.jar"]