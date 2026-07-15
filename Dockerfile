# Etapa 1: Construcción (Build)
# Usamos una imagen de Maven basada en Eclipse Temurin (Java 17)
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder
WORKDIR /app

# Copiamos el pom.xml y descargamos las dependencias
# Esto permite aprovechar la caché de Docker y no descargar todo en cada build si el POM no cambia
COPY pom.xml .
RUN mvn dependency:go-offline

# Copiamos el código fuente de nuestro Monolito Modular
COPY src ./src

# Compilamos el proyecto y generamos el JAR ejecutable
# El flag -DskipTests asume que los tests ya pasaron en la etapa de GitHub Actions
RUN mvn clean package -DskipTests

# Etapa 2: Ejecución (Run)
# Usamos una imagen JRE Alpine muy ligera (solo entorno de ejecución) para ahorrar espacio y RAM
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copiamos el JAR compilado desde la etapa de construcción 'builder'
COPY --from=builder /app/target/*.jar app.jar

# Exponemos el puerto estándar donde correrá Spring Boot
EXPOSE 8080

# Configuraciones JVM específicas para optimizar en Render (capa gratuita = 512MB RAM):
# -XX:+UseContainerSupport = Permite a la JVM leer límites de memoria de Docker (Render)
# -XX:MaxRAMPercentage=75.0 = La JVM usará como Heap hasta el 75% de los 512MB disponibles (~384MB)
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
