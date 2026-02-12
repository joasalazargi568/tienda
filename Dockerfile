# ==== Etapa 1: Build ====
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copia pom.xml e instala dependencias
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline

# Copia el código
COPY src ./src

# Compila el JAR
RUN mvn -q -DskipTests clean package

# ==== Etapa 2: Runtime ====
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copia el jar generado
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]