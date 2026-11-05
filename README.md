# Laboratorio U12: Dockerización con Perfiles de Spring Boot y Pipeline CI/CD con GitHub Actions

![Java](https://img.shields.io/badge/Java-21-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.5-brightgreen.svg)
![Docker](https://img.shields.io/badge/Docker-Multi--Stage-2496ED.svg)
![Actuator](https://img.shields.io/badge/Spring_Boot-Actuator-6DB33F.svg)
![License](https://img.shields.io/badge/License-Apache_2.0-lightgrey.svg)

---

## 1. Identificación del Proyecto

- **Repositorio GitHub:** [`AbrilProWeb-post1-u12`](https://github.com/ajaxmercury/AbrilProWeb-post1-u12)
- **Nombre de la Aplicación:** `mi-spring-app`
- **Paquete Raíz:** `com.universidad.app`
- **Plataforma:** Java 21, Spring Boot 3.2.5, Docker Multi-Stage (Alpine), GitHub Actions.
- **Autor / DevOps Engineer:** ajaxmercury

---

## 2. Instrucciones de Construcción y Ejecución Local con Docker

### 2.1 Construcción de la Imagen Docker
El proceso utiliza un `Dockerfile` multi-stage optimizado que aprovecha la caché de dependencias de Maven:

```bash
docker build -t mi-spring-app .
```

Verificación de la imagen generada:
```bash
docker images | grep mi-spring-app
```

---

### 2.2 Ejecución en Perfil de Desarrollo (`dev` - Por Defecto)
El perfil `dev` utiliza H2 en memoria, `ddl-auto=create-drop`, nivel de log `DEBUG` y exposición total de Actuator:

```bash
docker run -d -p 8080:8080 --name app-dev mi-spring-app
```

Verificación del contenedor en ejecución:
```bash
docker ps
docker logs -f app-dev
```

Prueba de salud en desarrollo:
```bash
curl -i http://localhost:8080/actuator/health
```

---

### 2.3 Ejecución en Perfil de Producción (`prod`) con Inyección de Variables
El perfil `prod` desactiva H2, activa el driver de PostgreSQL, valida el esquema con `ddl-auto=validate`, restringe logs a `WARN/INFO` y expone únicamente `health` e `info` en Actuator:

```bash
docker run -d -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_URL=jdbc:postgresql://postgres-host:5432/proddb \
  -e DB_USERNAME=prod_user \
  -e DB_PASSWORD=prod_secure_password \
  --name app-prod mi-spring-app
```

---

## 3. Decisiones de Diseño Técnico - Parte 1 (Docker y Perfiles)

### Decisión 1: Separación de Perfiles y Externalización con Placeholders (`${VAR:default}`)
- **Problema:** Cablear credenciales o configuraciones de base de datos en el código fuente vulnera la seguridad e impide trasladar el artefacto compilado entre diferentes entornos.
- **Solución:** Se implementó una jerarquía de configuración con `application.properties` (base con placeholders `${PORT:8080}` y `${SPRING_PROFILES_ACTIVE:dev}`), complementado por `application-dev.properties` (H2 volátil para pruebas ágiles) y `application-prod.properties` (PostgreSQL externalizado).
- **Justificación:** Cumple estrictamente con el principio III de *The Twelve-Factor App* (Configuración en el entorno) y el estándar *Build Once, Deploy Anywhere*: la misma imagen de Docker se promociona desde desarrollo local hasta producción sin recompilar.

### Decisión 2: Estrategia de Construcción Multi-Stage en Docker
- **Problema:** Una imagen que contenga el JDK completo, herramientas de compilación y el repositorio local de Maven pesa más de 600 MB y contiene binarios innecesarios en tiempo de ejecución.
- **Solución:** Se dividió el `Dockerfile` en dos etapas:
  1. `builder` (`eclipse-temurin:21-jdk-alpine`): Copia primero `pom.xml`, descarga dependencias con `mvn dependency:go-offline` para cachear la capa, copia `src/` y compila con `mvn clean package -DskipTests`.
  2. `runtime` (`eclipse-temurin:21-jre-alpine`): Contiene únicamente el JRE liviano y copia exclusivamente el archivo `app.jar` generado.
- **Justificación:** Reduce el tamaño de la imagen final a menos de 190 MB (más del 65% de reducción), acelera el despliegue en registries de contenedores y disminuye la superficie de ataque al no incluir compiladores ni herramientas de build en producción.

### Decisión 3: Ejecución de Contenedor con Usuario No-Root (Principio de Menor Privilegio)
- **Problema:** Los contenedores que ejecutan procesos como usuario `root` (UID 0) representan un riesgo de seguridad crítico (*container breakout*), donde una vulnerabilidad en la JVM podría otorgar acceso privilegiado al host subyacente.
- **Solución:** En la etapa runtime se crearon un grupo y un usuario de sistema dedicado sin privilegios mediante `addgroup -S spring && adduser -S spring -G spring`, estableciendo la directiva `USER spring`.
- **Justificación:** Alineación total con los estándares CIS Docker Benchmark y Kubernetes Pod Security Standards (PSS), garantizando que el proceso de la aplicación opere bajo el principio de mínimos privilegios indispensables.
