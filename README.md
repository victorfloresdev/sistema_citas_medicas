# MediGestión - Sistema de Gestión de Citas Médicas

Sistema empresarial en consola desarrollado en **Java 21**, **Hibernate 6 (JPA 3.1)** y conexión a **PostgreSQL**, con arquitectura desacoplada en capas: **Model (Entidades JPA) -> DAO (Persistencia genérica con Hibernate) -> Service (Lógica de Negocio y Reglas RF) -> Controller (Consola Interactiva)**.

---

## 1. Arquitectura y Patrones de Diseño
El proyecto sigue rigurosamente los principios de diseño de software empresarial:

* **Arquitectura en 4 Capas**:
  - `com.medigestion.model`: Mapeo ORM de las 12 tablas de la base de datos relacional de MediGestión con anotaciones Jakarta Persistence / Hibernate.
  - `com.medigestion.dao`: Patrón **Data Access Object (DAO)** mediante `GenericDAO<T, ID>` para operaciones CRUD atómicas y consultas especializadas HQL/JPQL con `JOIN FETCH`.
  - `com.medigestion.service`: Capa de negocio que centraliza las reglas de negocio (cálculo dinámico de congestión/afluencia, control de 1 cita semanal por especialidad, verificación de 60 min de anticipación para cancelaciones web, y registro de demanda no atendida).
  - `com.medigestion.controller`: Controladores de interacción por consola segmentados por rol (Paciente, Médico y Administrador), reflejando los flujos de los BPMNs y las pantallas del prototipo.
* **Patrón Singleton**: Implementado en `HibernateUtil` para la gestión centralizada del `EntityManagerFactory`.
* **Soporte Dual de Base de Datos**: Configurado primariamente para **PostgreSQL** (`jdbc:postgresql://localhost:5432/medigestion`). Cuenta con fallback transparente en memoria para garantizar que cualquier docente o evaluador pueda ejecutar y revisar todo el flujo sin depender de que el servicio local de PostgreSQL esté iniciado.

---

## 2. Cobertura de Requerimientos Funcionales y BPMNs

| Requerimiento / BPMN | Descripción | Implementación en el Sistema |
| :--- | :--- | :--- |
| **BPMN-1 / RF-01** | Autenticación ágil por DNI del asegurado | Si el DNI existe en la BD carga sus datos; si no existe, abre el formulario de registro ágil en consola. |
| **RF-02 / RF-03** | Dashboard de afluencia y cartelera | Cálculo en tiempo real de la densidad de citas del día vs capacidad, indicando nivel Bajo, Moderado o Alto. |
| **RF-04** | Búsqueda y reserva de cupos con reglas de negocio | Consulta filtrada por especialidad y médico dentro del horizonte de 30 días. Bloquea si el paciente ya tiene cita en la misma semana para esa especialidad. |
| **RF-05 / RF-08** | Emisión de Ticket Digital único y Notificación | Generación de ticket tipo `MG-XXXX-YY`, indicando consultorio y tolerancia previa de 15 min. Envío a canal Telegram del paciente. |
| **RF-07** | Registro de demanda no atendida | Si el paciente busca una especialidad sin cupos, se registra automáticamente en la tabla `demanda_no_atendida`. |
| **BPMN-2 / RF-10** | Cancelación y liberación inmediata de cupo | Permite cancelar citas liberando el cupo de inmediato. Si se cancela por canal WEB, exige un mínimo de 1 hora de anticipación. |
| **BPMN-3 / RF-13** | Estación médica y llamado a sala de espera | El médico llama al paciente a su consultorio (actualiza sala de espera), atiende la consulta (Completada) o marca Ausente si vence la tolerancia. |
| **BPMN-4 / RF-14** | Dashboard gerencial de administración | Métricas consolidadas de citas del día, total de médicos activos, cupos programados y porcentaje de ocupación. |

---

## 3. Datos Maestros Iniciales (Coincidentes con el Prototipo)
El semillero `DataSeeder` precarga automáticamente la información del diseño:
* **Especialidades**: Cardiología, Pediatría, Ortopedia, Neurología, Dermatología.
* **Médicos**:
  - Dra. Sarah Jenkins (Cardiología - Consultorio C-101)
  - Dr. Marcus Thorne (Cardiología - Consultorio C-102)
  - Dr. Michael Chen (Pediatría - Consultorio P-201)
  - Dra. Emily Rodriguez (Ortopedia - Consultorio O-301)
  - Dr. Robert Vance (Neurología - Consultorio N-105)
  - Dra. Anita Patel (Dermatología - Consultorio D-204)
* **Paciente Demo**: DNI `45892134` (Carlos Mendoza Rivera).

---

## 4. Instrucciones de Ejecución

### Opción 1: Ejecución directa (Windows)
Hacer doble clic en el archivo:
```text
ejecutar.bat
```

### Opción 2: Ejecución por consola mediante JAR autónomo
En la carpeta `sistema`:
```bash
java -jar target/medigestion-app.jar
```

### Opción 3: Recompilar con Maven Wrapper
```bash
.\mvnw.cmd clean package -DskipTests
java -jar target/medigestion-app.jar
```
