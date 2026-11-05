# =====================================================================
# DOCKERFILE MULTI-STAGE OPTIMIZADO - MI-SPRING-APP
# =====================================================================

# ---------------------------------------------------------------------
# ETAPA 1: BUILDER (Compilación y Empaquetado con Java 21)
# ---------------------------------------------------------------------
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /build

# Instalar Maven en Alpine
RUN apk add --no-cache maven

# 1. Copiar primero exclusivamente el descriptor pom.xml
COPY pom.xml .

# 2. Descargar dependencias para aprovechar la caché de capas de Docker
# Si el código fuente cambia pero el pom.xml no, este paso se reutiliza del caché
RUN mvn dependency:go-offline -q

# 3. Copiar el código fuente de la aplicación
COPY src ./src

# 4. Compilar y generar el archivo ejecutable (JAR) omitiendo pruebas en el build de imagen
RUN mvn clean package -DskipTests -q

# ---------------------------------------------------------------------
# ETAPA 2: RUNTIME (Entorno de Ejecución Seguro y Liviano con JRE 21)
# ---------------------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Principio de menor privilegio: Crear usuario y grupo de sistema 'spring' no-root
RUN addgroup -S spring && adduser -S spring -G spring

# Cambiar al usuario sin privilegios
USER spring

# Copiar exclusivamente el artefacto final generado en la etapa builder
COPY --from=builder /build/target/*.jar app.jar

# Exponer el puerto por defecto de la aplicación
EXPOSE 8080

# Comando de inicio del contenedor
ENTRYPOINT ["java", "-jar", "app.jar"]
