# MediGestión - Sistema de Gestión de Citas Médicas
## Entregable Académico: Avance de Desarrollo 40% (Semana 11)
### Curso Integrador I: Sistemas - Software (UTP)

---

## 1. Resumen Ejecutivo y Cumplimiento de la Rúbrica

El presente proyecto implementa el **40% del alcance funcional** del sistema **MediGestión**, enfocado en la completitud operativa del **Flujo Crítico de Solicitud y Reserva de Citas Médicas (BPM-1)**, la integración de recursos Java empresariales y la infraestructura de pruebas de caja blanca en la capa de servicios.

### Cumplimiento estricto de los criterios de evaluación:

| Criterio de la Rúbrica | Evidencia en el Proyecto | Ubicación / Implementación |
| :--- | :--- | :--- |
| **Diseño de la solución (MVC, TDD, DAO, SOLID y Seguridad)** | • Arquitectura en 4 capas (**Model -> DAO -> Service -> Controller**).<br>• Interfaces segregadas `IDAO<T,ID>`, `IReservaCitaDAO`, `IOfertaMedicaDAO`, etc. (**ISP, DIP, LSP**).<br>• Capa `Service` desacoplada con inyección de dependencias para TDD (**DIP**).<br>• Consultas HQL parametrizadas contra inyección SQL.<br>• Hashing irreversible de contraseñas con **BCrypt** (costo 12) con sal aleatoria (**RNF-04**).<br>• Control de acceso por DNI de sesión del asegurado. | `com.medigestion.model`<br>`com.medigestion.dao`<br>`com.medigestion.service`<br>`com.medigestion.controller` |
| **Uso de Recursos Java** | • **Google Guava**: Precondiciones defensivas (`Preconditions.checkNotNull`, `checkState`, `checkArgument`) y colecciones inmutables (`ImmutableList`).<br>• **Apache POI**: Generación y formateo de reportes ejecutivos en formato nativo Microsoft Excel (`.xlsx`) con estilos corporativos, cebra y fórmulas de conteo.<br>• **Apache Commons**: `StringUtils` (sanitización, padding, mayúsculas) y `EmailValidator` para verificación formal de correos según RFC.<br>• **Logback**: Configuración dual (consola interactiva limpia + archivo persistente `logs/medigestion.log`) con niveles `INFO` y `WARN` con timestamps. | `pom.xml`<br>`logback.xml`<br>`ReporteService.java`<br>`CitaService.java`<br>`PacienteService.java` |
| **Versionamiento con Git y GitHub** | • Historia de commits semánticos (*Conventional Commits*).<br>• Ramas de trabajo (`feature/bpm1-reserva-citas`, `feature/reportes-apache-poi`, `test/tdd-caja-blanca`).<br>• Tag de versión `v0.4.0-avance40`.<br>• Documentación del 100% de trazabilidad en la plantilla de Semana 11. | `Plantilla_Semana_11_Control_Versiones_Avance_40.xlsx`<br>Repositorio GitHub |
| **Alcance Acotado al 40%** | • Flujo principal 100% operativo en consola: **BPM-1 (P01 -> P02 -> P03 -> P04)**.<br>• Módulo de Reportería Excel 100% operativo: **REP-01 (R01, R02, R03)**.<br>• Delimitación explícita para evitar sobredesarrollo: Estación Médica (BPM-3) y Panel Administrador (BPM-4) programados para hitos posteriores (60% en Semana 13 y 80%-100% en Semanas 15-17). | `App.java`<br>`PacienteController.java`<br>`ReporteController.java` |
| **Pruebas de Caja Blanca en Service (TDD)** | • Suite de **16 pruebas unitarias automatizadas** con **100% de éxito**.<br>• Pruebas de caja blanca de caminos independientes (complejidad ciclomática de McCabe $V(G) = 4$) para `reservarCita()` y `cancelarCita()`.<br>• Aislamiento estricto de dependencias con **Mockito** (sin requerir base de datos activa). | `src/test/java/com/medigestion/service/CitaServiceTest.java`<br>`PacienteServiceTest.java`<br>`ReporteServiceTest.java` |

---

## 2. Delimitación del Alcance del 40% (Semana 11)

Para respetar rigurosamente la directiva académica de presentar **únicamente el 40% del sistema propuesto en consola**, el proyecto establece las siguientes fronteras funcionales:

```
┌────────────────────────────────────────────────────────────────────────┐
│                   MEDIGESTIÓN - ALCANCE DEL HITO 40%                   │
├──────────────────────────────────────────────────┬─────────────────────┤
│  MÓDULOS ACTIVOS Y 100% OPERATIVOS (40%)         │  ESTADO             │
├──────────────────────────────────────────────────┼─────────────────────┤
│  1. Portal del Paciente (BPM-1: Pantallas P01-P04)│  OPERATIVO EN CONSOLA│
│     - Identificación y registro ágil por DNI (RF-01)                    │
│     - Consulta de cartelera y nivel de afluencia (RF-02, RF-03)         │
│     - Búsqueda de cupos médicos horizonte 30 días (RF-03)               │
│     - Registro automático de demanda no atendida (RF-07)                │
│     - Validación de regla 1 cita semanal por especialidad (RF-04)       │
│     - Emisión de ticket digital único MG-XXXX-YY (RF-05, RF-08)         │
│     - Cancelación de cita con regla de 60 min de anticipación (RF-06)   │
│  2. Módulo de Reportes Oficiales en Excel (REP-01)│  OPERATIVO EN CONSOLA│
│     - Exportación de Reporte de Citas Programadas (.xlsx con POI)       │
│     - Exportación de Reporte de Demanda y Afluencia (.xlsx con POI)     │
├──────────────────────────────────────────────────┼─────────────────────┤
│  MÓDULOS DELIMITADOS PARA HITOS POSTERIORES       │  HITO PLANIFICADO   │
├──────────────────────────────────────────────────┼─────────────────────┤
│  • Estación Médica y Sala de Espera en vivo (BPM-3│  Hito 60% (Sem 13)  │
│    / Pantallas P06-P07 / RF-13)                  │  y 70% (Sem 14)     │
│  • Panel de Administración y Parametrización      │  Hito 80% (Sem 15)  │
│    Masiva de Agendas (BPM-4 / P08-P10 / RF-14)   │  a 100% (Sem 17)    │
└──────────────────────────────────────────────────┴─────────────────────┘
```

---

## 3. Matriz de Trazabilidad Integral

| Flujo / BPM | Requerimiento | Pantalla Prototipo | Capa Controller | Capa Service | Capa DAO / Persistencia | Tabla(s) BD | Recurso Java / Log | Prueba de Verificación |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **BPM-1** | **RF-01** Identidad del asegurado | P01 Formulario de Identidad | `PacienteController` | `PacienteService` | `PacienteDAO` (`IPacienteDAO`) | `pacientes` | Apache Commons + BCrypt + Logback | `PacienteServiceTest.testRegistrarPaciente_RegistroExitoso` |
| **BPM-1** | **RF-02 / RF-03** Cartelera y Afluencia | P02 Cartelera de Horarios | `PacienteController` | `CitaService` | `ReservaCitaDAO` (`IReservaCitaDAO`) | `reserva_citas`, `horarios` | Google Guava + Logback `INFO` | `CitaServiceTest.testCalcularAfluenciaActual` |
| **BPM-1** | **RF-03** Búsqueda de cupos médicos | P02 Cartelera de Horarios | `PacienteController` | `CitaService` | `OfertaMedicaDAO` (`IOfertaMedicaDAO`) | `oferta_medica`, `medicos` | Guava `ImmutableList` | `CitaServiceTest.testBuscarOfertasDisponibles` |
| **BPM-1** | **RF-07** Demanda no atendida | P02 Cartelera de Horarios | `PacienteController` | `CitaService` | `DemandaNoAtendidaDAO` | `demanda_no_atendida` | Logback `WARN` + `bitacora_eventos` | `CitaServiceTest.testBuscarOfertasDisponibles_RegistraDemandaNoAtendida` |
| **BPM-1** | **RF-04** Regla 1 cita semanal | P03 Selección y Confirmación | `PacienteController` | `CitaService` | `OfertaMedicaDAO` + `ReservaCitaDAO` | `oferta_medica`, `reserva_citas` | Guava `Preconditions.checkState` | `CitaServiceTest.testReservarCita_Camino3_ReglaUnaCitaSemanalViolada` |
| **BPM-1** | **RF-05 / RF-08** Ticket y Notificación | P03 Resumen y Constancia | `PacienteController` | `CitaService` | `ReservaCitaDAO` + `NotificacionService` | `reserva_citas`, `notificaciones` | Apache Commons + Logback `INFO` | `CitaServiceTest.testReservarCita_Camino4_ReservaExitosa` |
| **REP-01** | **R01, R02, R03** Reportes Excel | Menú Reportes / P10 | `ReporteController` | `ReporteService` | `ReservaCitaDAO` + `DemandaDAO` | `reserva_citas`, `demanda_no_atendida` | **Apache POI (XSSFWorkbook)** | `ReporteServiceTest.testExportarReporteCitasExcel_GeneracionValida` |
| **BPM-2** | **RF-06 / RF-10** Cancelación de Cita | P05 Mis Citas | `PacienteController` | `CitaService` | `ReservaCitaDAO` + `OfertaMedicaDAO` | `reserva_citas`, `oferta_medica` | Logback `INFO` + Bitácora | `CitaServiceTest.testCancelarCita_Camino4_CancelacionExitosa` |

---

## 4. Demostración y Uso de Recursos Java Obligatorios

### A. Google Guava
- **Validaciones de Invariantes y Precondiciones**: Se utiliza `Preconditions.checkNotNull`, `Preconditions.checkState` y `Preconditions.checkArgument` en `CitaService` y `PacienteService` para garantizar programación defensiva.
- **Colecciones Inmutables**: Métodos como `listarEspecialidades()` y `buscarOfertasDisponibles()` devuelven `ImmutableList.copyOf(...)` garantizando la inmutabilidad de los datos transferidos hacia el controlador.

### B. Apache POI (`poi` y `poi-ooxml`)
- **Exportación Nactiva de Libros Excel (.xlsx)**: Implementada en `ReporteService.java`.
- **Estilos y Formato Corporativo**:
  - Encabezado institucional fusionado con fondo azul marino (`DARK_BLUE`) y texto en negrita blanco.
  - Subtítulo con metadatos de auditoría y fecha/hora exacta de emisión.
  - Encabezados de columna con fondo gris y bordes delgados.
  - Datos tabulares con sombreado cebra alternado (`GREY_25_PERCENT`).
  - Formateo estricto de fechas y alineación centrada para tickets, DNI y consultorios.
  - Fila inferior de resumen con total de registros exportados y auto-ajuste de columnas (`autoSizeColumn`).
- **Archivos generados**:
  - `sistema/reportes/Reporte_Citas_Medicas.xlsx`
  - `sistema/reportes/Reporte_Demanda_Afluencia.xlsx`

### C. Apache Commons (`commons-lang3`, `commons-collections4`, `commons-validator`)
- **`StringUtils`**: Normalización y sanitización de entradas de texto, capitalización de nombres/apellidos (`StringUtils.capitalize`), justificación y relleno de tickets (`StringUtils.leftPad`), y enmascaramiento seguro de datos sensibles.
- **`EmailValidator`**: Verificación formal de sintaxis de correos electrónicos en `PacienteService.java` de acuerdo a los estándares RFC.

### D. Logback y SLF4J
- **Configuración centralizada**: En `src/main/resources/logback.xml`.
- **Estrategia de Log**:
  - Consola limpia con formato conciso para una experiencia de usuario óptima.
  - Archivo físico persistente en `sistema/logs/medigestion.log` con timestamps milimétricos, hilo, nivel (`INFO`, `WARN`, `ERROR`), clase emisora y mensaje detallado de auditoría para cada reserva, cancelación o alerta de seguridad.

---

## 5. Pruebas Funcionales de Caja Blanca en la Capa Service

Se aplicó la metodología de **Pruebas de Caja Blanca** sobre los métodos centrales de la capa `Service`, basándose en el análisis del **Grafo de Flujo de Control (CFG)** y la **Complejidad Ciclomática de McCabe**:

$$V(G) = E - N + 2P = 4 \text{ caminos independientes}$$

### Prueba de Caja Blanca 1: `CitaService.reservarCita(paciente, idOferta)`
1. **Camino 1 (Oferta inexistente)**: El ID de oferta no existe en la persistencia $\rightarrow$ Lanza `IllegalArgumentException`.
2. **Camino 2 (Oferta no disponible)**: El horario ya cuenta con estado distinto a 'Disponible' $\rightarrow$ Lanza `IllegalStateException` y no persiste cita.
3. **Camino 3 (Violación regla semanal RF-04)**: El asegurado ya tiene una cita activa en la misma semana para esa especialidad médica $\rightarrow$ Lanza `IllegalStateException` y bloquea la operación.
4. **Camino 4 (Reserva exitosa)**: Horario disponible y regla semanal satisfecha $\rightarrow$ Genera ticket con prefijo `MG-`, cambia estado de oferta a 'Reservado', persiste la `ReservaCita`, despacha notificación y registra bitácora.

### Prueba de Caja Blanca 2: `CitaService.cancelarCita(idReserva, canal, motivo)`
1. **Camino 1 (Reserva inexistente)**: ID de reserva no encontrado $\rightarrow$ Lanza `IllegalArgumentException`.
2. **Camino 2 (Cita ya cancelada)**: Estado actual de la reserva es 'Cancelada' $\rightarrow$ Lanza `IllegalStateException`.
3. **Camino 3 (Restricción 60 min en canal WEB - RF-06)**: Cancelación solicitada por canal 'WEB' a menos de 60 minutos de la cita $\rightarrow$ Lanza `IllegalStateException` exigiendo trámite presencial en ventanilla.
4. **Camino 4 (Cancelación exitosa)**: Cancelación con más de 60 min $\rightarrow$ Estado de cita cambia a 'Cancelada', cupo de `OfertaMedica` se libera de inmediato a 'Disponible', se actualiza en base de datos y se registra auditoría.

---

## 6. Guía de Ejecución Paso a Paso

### Requisitos Previos:
- **Java Development Kit (JDK) 21** o superior instalado y configurado en el `PATH` (`java -version`).
- *(Opcional)* **PostgreSQL** corriendo en `localhost:5432` con la base `medigestion`. 
  > **Nota de Resiliencia**: Si PostgreSQL no está encendido, el sistema conmuta de forma **100% automática y transparente** a una base de datos en memoria (`H2` en modo de compatibilidad PostgreSQL), permitiendo evaluar todo el sistema sin instalaciones adicionales.

---

### Opción 1: Ejecución Directa mediante Script (Recomendada para Windows)
Hacer doble clic en el archivo ubicado en la raíz de `sistema`:
```text
ejecutar.bat
```

---

### Opción 2: Ejecución mediante el JAR Autónomo (Fat JAR con Dependencias)
Abrir una terminal de comandos en la carpeta `sistema` y ejecutar:
```bash
java -jar target/medigestion-app.jar
```

---

### Opción 3: Compilación, Pruebas y Ejecución Completa desde Código Fuente
```bash
# 1. Limpiar y compilar el proyecto
.\mvnw.cmd clean compile

# 2. Ejecutar la suite completa de pruebas unitarias y de caja blanca (16 tests)
.\mvnw.cmd test

# 3. Empaquetar el JAR ejecutable con todas las librerías
.\mvnw.cmd package -DskipTests

# 4. Iniciar la aplicación
java -jar target/medigestion-app.jar
```

---

## 7. Verificación de Evidencias y Salidas del Sistema

Tras ejecutar la aplicación y realizar reservas o emitir reportes:
1. **Libros Excel generados (Apache POI)**:
   - `sistema/reportes/Reporte_Citas_Medicas.xlsx`
   - `sistema/reportes/Reporte_Demanda_Afluencia.xlsx`
2. **Registro persistente de Logs y Auditoría (Logback)**:
   - `sistema/logs/medigestion.log`
3. **Reportes de ejecución de pruebas automatizadas**:
   - `sistema/target/surefire-reports/`
4. **Hojas de cálculo de trazabilidad y gestión completadas**:
   - `../CISS_APF3/Matriz_Trazabilidad_y_Transferencia_CitaLab.xlsx`
   - `../CISS_APF3/Plantilla_Semana_9_Arquitectura_Proyecto_Integrador.xlsx`
   - `../CISS_APF3/Plantilla_Semana_11_Control_Versiones_Avance_40.xlsx`

---

## 8. Datos Maestros Preconfigurados para Demostración

El semillero `DataSeeder` precarga automáticamente:
- **Paciente de Prueba**: DNI `45892134` (Carlos Mendoza Rivera).
- **Especialidades Médicas**: Cardiología, Pediatría, Ortopedia, Neurología, Dermatología.
- **Médicos Asignados**:
  - Dra. Sarah Jenkins (Cardiología - Consultorio C-101)
  - Dr. Marcus Thorne (Cardiología - Consultorio C-102)
  - Dr. Michael Chen (Pediatría - Consultorio P-201)
  - Dra. Emily Rodriguez (Ortopedia - Consultorio O-301)
  - Dr. Robert Vance (Neurología - Consultorio N-105)
  - Dra. Anita Patel (Dermatología - Consultorio D-204)
