# Stage 1: Build the application
FROM maven:3.9.6-eclipse-temurin-17-focal AS build
WORKDIR /app
# Copy only the pom.xml first to cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline
# Copy the source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:17-jre-focal
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
