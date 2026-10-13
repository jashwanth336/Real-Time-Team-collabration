FROM maven:3.9.11-eclipse-temurin-25 AS build

WORKDIR /app
COPY backend/pom.xml ./pom.xml
RUN mvn dependency:go-offline
COPY backend/src ./src
RUN mvn -DskipTests package

FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
