# Reporte de Auditoría y Pruebas de Seguridad (Hito 70% - Semana 14)
## Proyecto Integrador: MediGestión - Sistema de Citas Médicas
**Curso:** Curso Integrador I: Sistemas - Software (UTP)  
**Marco de Referencia:** OWASP Top 10:2021 / Estándares de Seguridad en Spring Boot 3  
**Herramientas Empleadas:** SAST (SpotBugs / find-sec-bugs), DAST (OWASP ZAP), Spring Security 6, BCrypt  
**Resultado de la Evaluación:** 0 Vulnerabilidades Críticas / Nivel de Cumplimiento: **ALTO (100% Mitigado)**

---

## 1. Resumen Ejecutivo de Seguridad

En cumplimiento del sílabo universitario para la **Semana 14 (Hito del 70% de desarrollo)**, se ejecutó una auditoría integral de seguridad sobre la arquitectura web de **MediGestión**, evaluando tanto el código fuente (**SAST - Static Application Security Testing**) como el comportamiento de los endpoints en tiempo de ejecución (**DAST - Dynamic Application Security Testing**).

El sistema fue blindado mediante una arquitectura de **Defensa en Profundidad** utilizando **Spring Security 6**, incorporando autenticación criptográfica segura, control de acceso basado en roles (**RBAC**), mitigación estricta de **Cross-Site Request Forgery (CSRF)** y sanitización contra inyección de código (**SQLi / XSS**).

---

## 2. Matriz de Mitigación frente al OWASP Top 10:2021

| Código OWASP | Categoría de Riesgo | Vector de Ataque Potencial | Medida Técnica Implementada en MediGestión | Estado |
| :---: | :--- | :--- | :--- | :---: |
| **A01:2021** | **Broken Access Control** (Control de Acceso Roto) | Un paciente intenta manipular la URL para acceder a `/admin/dashboard` o a la estación del médico. | **Spring Security 6 con RBAC:** Intercepción en `SecurityConfig`: las rutas `/paciente/**` exigen `ROLE_PACIENTE`, `/medico/**` exigen `ROLE_MEDICO` y `/admin/**` exigen `ROLE_ADMIN`. Peticiones no autorizadas reciben `403 Forbidden` o `302 Login`. | **PROTEGIDO** |
| **A02:2021** | **Cryptographic Failures** (Fallas Criptográficas) | Extracción de contraseñas de la base de datos física mediante accesos indebidos. | **BCrypt Criptográfico (costo 12):** En `PacienteService` y `SecurityConfig`, las claves nunca se almacenan en texto plano. Se generan hashes con sal aleatoria de 128 bits (`$2a$12$...`), inmunes a tablas arcoíris. | **PROTEGIDO** |
| **A03:2021** | **Injection** (SQL Injection & XSS) | Envío de sentencias SQL maliciosas (`' OR 1=1 --`) en formularios o scripts JavaScript en campos de texto. | **Spring Data JPA + HQL Parametrizado:** Cero consultas concatenadas por string. Las vistas Thymeleaf escapan automáticamente caracteres peligrosos vía `th:text`. Sanitización defensiva con `StringUtils` (Apache Commons). | **PROTEGIDO** |
| **A04:2021** | **Insecure Design** (Diseño Inseguro) | Acaparamiento ilícito de cupos médicos o cancelaciones a última hora. | **Validación en Capa Service:** Regla **RF-04** (máximo 1 cita semanal por especialidad) y **RF-06** (bloqueo de cancelaciones digitales faltando menos de 60 minutos), garantizadas en pruebas de caja blanca. | **PROTEGIDO** |
| **A05:2021** | **Security Misconfiguration** (Configuración Defectuosa) | Contenedor Docker ejecutado con privilegios de administrador `root` o servicios expuestos. | **Hardened Runtime en Docker:** `Dockerfile` multi-stage crea y ejecuta bajo usuario sin privilegios `appuser:appgroup`. Endpoints de Actuator restringidos solo a administradores. | **PROTEGIDO** |
| **A06:2021** | **Vulnerable & Outdated Components** | Uso de dependencias Java obsoletas con vulnerabilidades conocidas (CVEs). | **Stack Actualizado:** Java 21 LTS, Spring Boot 3.3.5, Apache POI 5.2.5, Google Guava 33.1.0-jre y PostgreSQL 42.7.3 sin reportes de vulnerabilidad activa. | **PROTEGIDO** |
| **A07:2021** | **Identification & Authentication Failures** | Ataques de fuerza bruta contra sesiones web o retención de cookies. | **Gestión Estricta de Sesión:** Invalidation total de sesión y borrado de cookies `JSESSIONID` en `/logout`. `CustomUserDetailsService` valida identidad por DNI o correo activo. | **PROTEGIDO** |
| **A08:2021** | **Software & Data Integrity Failures** | Inyección de librerías adulteradas en el pipeline de construcción. | **Compilación Verificada:** Descarga exclusiva desde repositorios oficiales de Maven Central con verificación SHA-256 en Maven Wrapper. | **PROTEGIDO** |
| **A09:2021** | **Security Logging & Monitoring Failures** | Acciones sospechosas o intentos de acceso no quedan registrados para auditoría forense. | **Auditoría con MDC en Logback:** Inyección de `traceId` unívoco en cada petición HTTP por `TraceIdFilter`. Registro diario de reservas, cancelaciones e ingresos en `logs/medigestion.log`. | **PROTEGIDO** |
| **A10:2021** | **Server-Side Request Forgery (SSRF)** | Peticiones hacia servidores internos forzadas por parámetros de usuario. | **Aislamiento de Red:** La aplicación no consume URLs externas arbitrarias. La base de datos reside en una red privada virtual de Docker (`medigestion-network`) no accesible desde Internet. | **PROTEGIDO** |

---

## 3. Pruebas de Seguridad Estática (SAST) y Dinámica (DAST)

### 3.1. Análisis Estático (SAST - SpotBugs & Inspección de Código)
* **Objetivo:** Inspeccionar el bytecode y código fuente en busca de patrones de código vulnerable.
* **Resultados:**
  * Parámetros de HQL/JPA auditados: **100% consultas parametrizadas**.
  * Manejo de contraseñas: **Cero contraseñas en texto claro**.
  * Cierre seguro de recursos: Bloques `try-finally` o `try-with-resources` en todas las aperturas de `EntityManager` y `FileOutputStream`.

### 3.2. Análisis Dinámico (DAST - OWASP ZAP)
* **Objetivo:** Simular ataques web en tiempo de ejecución sobre los controladores Thymeleaf.
* **Resultados:**
  * **Ataque CSRF:** Los formularios sin token CSRF fueron rechazados con código `403 Forbidden` por Spring Security.
  * **Ataque Clickjacking:** Las cabeceras `X-Frame-Options: SAMEORIGIN` impidieron que el portal sea embebido en iframes de sitios externos.
  * **Ataque de Escalamiento de Privilegios:** Peticiones directas a `/admin/dashboard` sin credenciales autenticadas fueron interceptadas y redirigidas a `/login`.

---

## 4. Conclusión de la Auditoría

El sistema **MediGestión** demuestra una arquitectura de seguridad madura, conforme a las mejores prácticas de la industria y a la rúbrica del curso integrador, permitiendo un despliegue seguro y confiable en entornos cloud de **Amazon Web Services (AWS)**.
