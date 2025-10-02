# Build stage
FROM maven:3.9.5-eclipse-temurin-22 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:22-jre-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar /app/goDelivery.jar
EXPOSE 8085
CMD ["java", "-jar", "/app/goDelivery.jar"]