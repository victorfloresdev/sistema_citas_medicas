# ==============================================================================
# DOCKERFILE MULTI-STAGE: MEDIGESTIÓN SPRING BOOT 3 (HITO 80% - DESPLIEGUE AWS)
# ==============================================================================

# STAGE 1: Compilación y empaquetado seguro con Maven
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Copiar pom.xml y descargar dependencias para aprovechar caché de capas Docker
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiar código fuente y compilar
COPY src ./src
RUN mvn clean package -DskipTests

# STAGE 2: Imagen de ejecución ligera y endurecida (Hardened Runtime)
FROM eclipse-temurin:21-jre-alpine AS runner
WORKDIR /app

# Crear usuario sin privilegios root por seguridad (OWASP A05)
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser:appgroup

# Copiar solo el artefacto final empaquetado desde la etapa de compilación
COPY --from=builder /app/target/medigestion-citas-1.0-SNAPSHOT.jar app.jar

# Variables de entorno por defecto
ENV PORT=8080
ENV SPRING_PROFILES_ACTIVE=aws
EXPOSE 8080

# Healthcheck interno del contenedor
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
