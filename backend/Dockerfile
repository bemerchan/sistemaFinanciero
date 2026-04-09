FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B --no-transfer-progress
COPY src ./src
RUN mvn clean package -DskipTests --no-transfer-progress

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S spring && adduser -S spring -G spring
COPY --from=build /app/target/financial-system-0.0.1-SNAPSHOT.jar app.jar
USER spring
EXPOSE 9000
ENTRYPOINT ["java", "-Xmx512m", "-Xms256m", "-jar", "app.jar"]
