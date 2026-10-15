FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/WS_Central.jar app.jar
RUN mkdir -p /app/data
ENV DB_PATH=/app/data/central.db
CMD ["java", "-jar", "app.jar"]
