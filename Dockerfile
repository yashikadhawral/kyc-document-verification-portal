FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/kyc-portal.war app.war
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.war"]