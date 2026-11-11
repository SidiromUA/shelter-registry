# ЛР6. Багатоетапне збирання образу.
# Етап 1: збирання jar за допомогою Maven
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /src
COPY pom.xml .
RUN mvn -q dependency:go-offline           # залежності кешуються окремим шаром
COPY src ./src
RUN mvn -q package -DskipTests

# Етап 2: мінімальний образ лише з JRE і готовим jar
FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system --uid 1001 app        # не запускаємо застосунок від root
COPY --from=build /src/target/shelter-registry-*.jar app.jar
USER app
EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=prod
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
