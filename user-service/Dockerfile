FROM eclipse-temurin:17-jre
WORKDIR /into-the-sky
COPY --from=build /target/user-service-*.jar user-service.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "user-service.jar"]