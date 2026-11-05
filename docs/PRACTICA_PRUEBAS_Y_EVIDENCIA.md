# Práctica · Prueba y Evidencia sobre el Proyecto Integrador
## Sistema MediGestión - Gestión Integral de Citas Médicas
**Curso:** Curso Integrador I: Sistemas - Software (UTP)  
**Rama de Trabajo:** `test/practica-pruebas-evidencia`  
**Commit de Referencia:** `b57b8b5` / `36e8a7a`  
**Fecha de Ejecución:** 05 de Noviembre de 2026  
**Entorno de Ejecución:** Windows 11, Oracle JDK 25 LTS, Apache Maven 3.9.6, JUnit 5.10.2, Mockito 5.11.0, AssertJ 3.25.3  

---

## 1. Resumen Ejecutivo de la Práctica

La presente actividad tiene como objetivo diseñar formalmente **4 casos de prueba funcionales y de negocio** para el proyecto integrador **MediGestión**, seleccionando y priorizando el flujo de **mayor riesgo operativo y transaccional**: la **Reserva y Asignación de Cupos Médicos (BPM-1)**.

Se implementó de forma automatizada el caso de prueba priorizado (**CP-MED-01**) en código Java ejecutable bajo arquitectura TDD desacoplada (JUnit 5 + Mockito + AssertJ), ejecutándose exitosamente junto a la suite completa de pruebas del sistema (20 tests con 100% PASS). Además, se documentó de manera transparente la detección de un defecto técnico real (DEF-TEST-001) relativo al agente de byte-buddy en entornos JDK 25 modernos, su solución técnica inmediata y la posterior re-prueba exitosa.

---

## 2. Análisis de Riesgos y Justificación de la Priorización

En un sistema hospitalario de alta concurrencia, el mayor riesgo funcional reside en la **reserva no controlada de cupos médicos**, la cual puede provocar saturación deliberada o desabastecimiento de especialistas:

| Caso de Prueba | Flujo / Requisito | Probabilidad de Fallo | Impacto Operativo | Nivel de Riesgo | Prioridad de Automatización |
| :--- | :--- | :---: | :---: | :---: | :---: |
| **CP-MED-01** | **RF-04: Bloqueo de 1 Cita Semanal por Especialidad** | Media | **Crítico** (Acaparamiento de citas, desatención de otros asegurados) | **ALTO (9/10)** | **PRIORIZADO (Implementación Automatizada Exhaustiva)** |
| **CP-MED-02** | **RF-03/RF-05: Concurrencia y Cupo Ya Ocupado** | Media | **Crítico** (Overbooking, doble citación al mismo consultorio/horario) | **ALTO (8/10)** | Automatizado en suite de servicios |
| **CP-MED-03** | **RF-06: Restricción Temporal de Cancelación (< 60 min)** | Alta | Moderado (Pérdida de disponibilidad médica sin tiempo de reasignación) | **MEDIO-ALTO (7/10)** | Automatizado en suite de servicios |
| **CP-MED-04** | **RF-07: Registro Automático de Demanda No Atendida** | Baja | Moderado (Falta de visibilidad directiva sobre brecha de oferta hospitalaria) | **MEDIO (6/10)** | Automatizado en suite de servicios |

**Justificación de priorización de CP-MED-01:**  
La regla de negocio RF-04 asegura el principio de equidad distributiva de la red de salud. Si un asegurado puede reservar múltiples citas de Cardiología en la misma semana, desabastece a otros pacientes con patologías de riesgo. Probar este flujo con condiciones límite (misma semana vs. semana subsiguiente vs. distinta especialidad) constituye la validación más crítica de la lógica de negocio del sistema.

---

## 3. Especificación Formal de los 4 Casos de Prueba (Estándar IEEE 829 / ISTQB)

### Caso de Prueba 1: CP-MED-01 (Priorizado)
* **Identificador:** `CP-MED-01`
* **Nombre:** Validación y Bloqueo de Cita Duplicada en la Misma Semana por Especialidad (RF-04)
* **Flujo BPMN:** BPM-1: Solicitud y Reserva de Citas Médicas
* **Tipo de Prueba:** Funcional / Caja Blanca / Automatizada (Unit & Integration Service)
* **Objetivo:** Verificar que el sistema rechace con excepción controlada cualquier intento de un asegurado de registrar una segunda cita para la misma especialidad dentro de la misma semana calendario (lunes 00:00:00 a domingo 23:59:59).
* **Precondiciones:**
  1. Paciente con DNI `45892134` registrado y activo.
  2. Existe una cita confirmada previa para el paciente en la especialidad `Cardiología` el día miércoles de la semana en curso.
  3. Existe una oferta médica disponible (ID `501`) para `Cardiología` el día viernes de la misma semana.
* **Datos de Entrada:**
  * Paciente: DNI `45892134`
  * ID Oferta Médica: `501` (Cardiología - Viernes 10:00 hrs)
* **Pasos de Ejecución:**
  1. Iniciar el servicio `CitaService.reservarCita(paciente, 501)`.
  2. El sistema valida existencia de la oferta y su estado `Disponible`.
  3. El sistema calcula el rango semanal (lunes a domingo) de la cita solicitada.
  4. El sistema consulta `reservaCitaDAO.countCitasActivasSemanaPorEspecialidad()`.
  5. El sistema detecta conteo $\ge 1$.
* **Resultado Esperado:**
  * Se lanza excepción `IllegalStateException` con mensaje: *"Regla de negocio: El paciente 45892134 ya cuenta con una cita activa para la especialidad Cardiología en la misma semana."*.
  * La oferta médica `501` se mantiene intacta en estado `Disponible`.
  * No se guarda ningún registro en la tabla `reserva_citas`.
  * No se emite ticket ni se envía notificación vía Telegram/Email.
  * Se genera una advertencia en el log con nivel `WARN`.

---

### Caso de Prueba 2: CP-MED-02
* **Identificador:** `CP-MED-02`
* **Nombre:** Control de Concurrencia y Rechazo de Reserva sobre Cupo No Disponible (RF-03, RF-05)
* **Flujo BPMN:** BPM-1: Solicitud y Reserva de Citas Médicas
* **Tipo de Prueba:** Funcional / Concurrencia / Automatizada
* **Objetivo:** Garantizar la integridad transaccional evitando el *overbooking* o doble reserva cuando un horario ya fue tomado por otro usuario o marcado como `Reservado`.
* **Precondiciones:**
  1. Oferta Médica ID `500` con estado actual `Reservado`.
* **Datos de Entrada:**
  * Paciente: DNI `45892134`
  * ID Oferta Médica: `500`
* **Pasos de Ejecución:**
  1. Ejecutar `CitaService.reservarCita(paciente, 500)`.
  2. El sistema recupera los detalles de la oferta.
  3. Verifica la precondición de disponibilidad mediante Google Guava `Preconditions.checkState`.
* **Resultado Esperado:**
  * Excepción `IllegalStateException` indicando que el horario ya no está disponible.
  * Cero registros guardados en `reserva_citas` y cero notificaciones emitidas.

---

### Caso de Prueba 3: CP-MED-03
* **Identificador:** `CP-MED-03`
* **Nombre:** Restricción de Cancelación Web con Anticipación Menor a 60 Minutos (RF-06, RF-10)
* **Flujo BPMN:** BPM-2: Cancelación de Cita Médica
* **Tipo de Prueba:** Funcional / Regla Temporal / Automatizada
* **Objetivo:** Verificar que el sistema impida la cancelación por canal digital si la hora de inicio de la cita se encuentra a menos de 60 minutos de la hora del sistema, exigiendo trámite presencial en ventanilla.
* **Precondiciones:**
  * Reserva médica activa ID `801` (Ticket `MG-TEST-01`).
  * Fecha y hora de inicio de la cita: `LocalDateTime.now().plusMinutes(35)`.
* **Datos de Entrada:**
  * ID Reserva: `801`
  * DNI Paciente: `45892134`
  * Canal: `WEB`
  * Motivo: "Retraso de transporte"
* **Pasos de Ejecución:**
  1. Ejecutar `CitaService.cancelarCita(801, "45892134", "Retraso de transporte")`.
  2. El sistema calcula `Duration.between(LocalDateTime.now(), fechaHoraInicio).toMinutes()`.
  3. Compara si los minutos restantes son menores a 60.
* **Resultado Esperado:**
  * Se lanza `IllegalStateException` con la advertencia de que restan menos de 60 minutos y debe acudir a ventanilla.
  * El estado de la reserva permanece en `Confirmada`.
  * La oferta médica asociada permanece en `Reservado`.
  * Se genera log `WARN` con el evento.

---

### Caso de Prueba 4: CP-MED-04
* **Identificador:** `CP-MED-04`
* **Nombre:** Registro Automático de Demanda No Atendida ante Ausencia de Cupos Médicos (RF-07)
* **Flujo BPMN:** BPM-1: Consulta de Oferta Médica y Cartelera
* **Tipo de Prueba:** Funcional / Auditoría y Business Intelligence / Automatizada
* **Objetivo:** Validar que al buscar horarios para una especialidad que no cuente con cupos disponibles dentro de los próximos 30 días, el sistema capture de forma transparente la solicitud insatisfecha en la tabla `demanda_no_atendida`.
* **Precondiciones:**
  * Especialidad ID `3` ("Pediatría") sin ofertas en estado `Disponible` en el rango `[hoy, hoy + 30 días]`.
* **Datos de Entrada:**
  * DNI Paciente: `45892134`
  * ID Especialidad: `3`
* **Pasos de Ejecución:**
  1. Ejecutar `CitaService.buscarOfertasDisponibles(3, "45892134")`.
  2. `OfertaMedicaDAO` devuelve lista vacía `Collections.emptyList()`.
  3. El servicio detecta lista vacía e invoca `DemandaNoAtendidaDAO.save()`.
* **Resultado Esperado:**
  * El método retorna lista inmutable vacía `ImmutableList.of()`.
  * Se verifica la invocación a `demandaDAO.save()` con el DNI del paciente, ID de especialidad, canal "Consola" y timestamp actual.
  * Se emite log `WARN` de demanda insatisfecha.

---

## 4. Implementación del Caso Priorizado Automatizado

Se creó la clase de prueba unitaria y funcional dedicada:  
[`src/test/java/com/medigestion/service/ReservaCitaPrioritariaAutomatedTest.java`](file:///c:/Users/victo/UTP - Google Drive/Ciclo VI - Agosto-26/CISS/CISS_PROY/sistema/src/test/java/com/medigestion/service/ReservaCitaPrioritariaAutomatedTest.java)

### Sub-escenarios implementados para CP-MED-01:
1. **`cpMed01_DebeBloquearSegundaCitaMismaSemana` (Caso Negativo / Regla Bloqueante):**
   * Simula 1 cita activa previa en la semana para Cardiología.
   * Ejecuta la reserva y comprueba que se lance `IllegalStateException`.
   * Verifica que la oferta no pase a `Reservado` y no se guarde la reserva en BD (`verify(never())`).
2. **`cpMed01_DebePermitirCitaEnSemanaSiguiente` (Caso Positivo / Límite Temporal):**
   * Comprueba que si la cita pertenece a la semana siguiente (+8 días), el sistema la autoriza exitosamente.
   * Valida la emisión del ticket único `MG-XXXX-YY`, actualización de estado a `Confirmada` y despacho de notificación.
3. **`cpMed01_DebePermitirCitaMismaSemanaDiferenteEspecialidad` (Caso Positivo / Aislamiento de Especialidades):**
   * Comprueba que tener una cita en Cardiología no bloquea agendar en Odontología durante la misma semana.
4. **`cpMed01_DebeFallarConPrecondicionInvalida` (Caso Defensivo):**
   * Verifica precondiciones nulas de paciente o ID de oferta mediante Google Guava (`NullPointerException`).

---

## 5. Registro y Gestión de Defectos (Defect Tracking)

Siguiendo la directriz de la práctica de registrar cualquier `FAIL` o error técnico con total transparencia:

```
┌────────────────────────────────────────────────────────────────────────┐
│                        REGISTRO FORMAL DE DEFECTO                      │
├──────────────────────┬─────────────────────────────────────────────────┤
│ ID de Defecto        │ DEF-TEST-001                                    │
│ Severidad            │ ALTA (Bloqueo de ejecución de pruebas en JDK 25)│
│ Componente / Capa    │ Infraestructura de Pruebas (Mockito / Surefire) │
│ Detectado en Commit  │ 74378f1 (Versión inicial v0.4.0)                │
│ Fecha Detección      │ 05/11/2026 11:30 hrs                            │
├──────────────────────┴─────────────────────────────────────────────────┤
│ Descripción del Fallo:                                                 │
│ Al ejecutar 'mvn test' bajo Oracle JDK 25 LTS, Mockito 5.11 falló con  │
│ MockitoException: "Could not modify all classes [NotificacionService]" │
│ debido a las restricciones de retransformación de clases del agente    │
│ de instrumentación dinámica ByteBuddy en versiones modernas de Java.  │
├────────────────────────────────────────────────────────────────────────┤
│ Causa Raíz:                                                            │
│ El mock-maker predeterminado de Mockito 5 (inline mock maker) requiere  │
│ redefinición dinámica de bytecode en clases concretas. En JDK 25, esta │
│ operación es bloqueada por seguridad sin banderas de JVM adicionales.  │
├────────────────────────────────────────────────────────────────────────┤
│ Acción Correctiva Aplicada:                                            │
│ Se implementó el archivo de configuración SPI de Mockito:              │
│ 'src/test/resources/mockito-extensions/org.mockito.plugins.MockMaker'  │
│ declarando 'mock-maker-subclass', permitiendo generar mocks limpios    │
│ basados en subclases dinámicas compatibles al 100% con Java 21 y 25.   │
├────────────────────────────────────────────────────────────────────────┤
│ Resultado de la Re-prueba:                                             │
│ PASSED (0 errores, 0 fallos, 20/20 pruebas exitosas).                  │
│ Estado actual: CERRADO / SUBSANADO.                                    │
└────────────────────────────────────────────────────────────────────────┘
```

---

## 6. Resultados y Evidencia de Ejecución

### Resumen Consolidado de Casos de Prueba

| ID Caso | Requisito | Descripción | Tipo | Resultado | Tiempo |
| :---: | :---: | :--- | :---: | :---: | :---: |
| **CP-MED-01** | RF-04 | Bloqueo de cita duplicada en la misma semana por especialidad | Automatizada (Unit/Mock) | **PASS** | 2.439 s |
| **CP-MED-02** | RF-03 / RF-05 | Rechazo por concurrencia sobre oferta médica no disponible | Automatizada (Unit/Mock) | **PASS** | 0.035 s |
| **CP-MED-03** | RF-06 / RF-10 | Bloqueo de cancelación web faltando < 60 minutos | Automatizada (Unit/Mock) | **PASS** | 0.028 s |
| **CP-MED-04** | RF-07 | Registro automático de demanda no atendida en cupos agotados | Automatizada (Unit/Mock) | **PASS** | 0.041 s |

### Evidencia de la Consola de Maven (Surefire Reports)

```text
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.medigestion.service.CitaServiceTest
2026-11-05 11:35:07.024 [main] INFO  com.medigestion.service.CitaService - [RF-03 Afluencia] Calculado: 15% con 5 citas activas hoy (Capacidad: 50)
2026-11-05 11:35:07.078 [main] WARN  com.medigestion.service.CitaService - [RF-07 Demanda No Atendida] Demanda no atendida registrada para paciente 45892134 en especialidad: Pediatría
2026-11-05 11:35:07.118 [main] INFO  com.medigestion.service.CitaService - [RF-06 Cancelación Exitosa] Cita MG-OKAY-99 cancelada vía WEB. Cupo liberado inmediatamente a oferta general.
2026-11-05 11:35:07.231 [main] INFO  com.medigestion.service.CitaService - [RF-05 Ticket Emitido] Cita confirmada con Ticket MG-6LKE-0511 para paciente 45892134 en consultorio C-101
2026-11-05 11:35:07.247 [main] WARN  com.medigestion.service.CitaService - [RF-04 Bloqueo Cita Duplicada] Regla de negocio: El paciente 45892134 ya cuenta con una cita activa para la especialidad Cardiología en la misma semana.
2026-11-05 11:35:07.272 [main] WARN  com.medigestion.service.CitaService - [RF-06 Regla 60 Min] Cita MG-TEST-01: Cancelación denegada vía WEB: Faltan menos de 60 minutos para la cita. Debe realizar la cancelación justificada de manera presencial en ventanilla.
[INFO] Tests run: 10, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 2.447 s -- in com.medigestion.service.CitaServiceTest
[INFO] Running com.medigestion.service.PacienteServiceTest
2026-11-05 11:35:07.345 [main] WARN  c.m.service.PacienteService - [RF-01 Registro] El paciente con DNI 45892134 ya se encuentra registrado.
2026-11-05 11:35:08.006 [main] INFO  c.m.service.PacienteService - [RF-01 Registro Exitoso] Nuevo paciente registrado con éxito: DNI 77889900 (Ana maria Torres lopez)
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 2.068 s -- in com.medigestion.service.PacienteServiceTest
[INFO] Running com.medigestion.service.ReporteServiceTest
2026-11-05 11:35:11.565 [main] INFO  c.medigestion.service.ReporteService - [REP-01 Apache POI] Reporte de Citas generado exitosamente: target\test-reports\Reporte_Test_Citas.xlsx (Total filas: 1)
2026-11-05 11:35:11.923 [main] INFO  c.medigestion.service.ReporteService - [REP-01 Apache POI] Reporte de Demanda generado exitosamente: target\test-reports\Reporte_Test_Demanda.xlsx
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 2.630 s -- in com.medigestion.service.ReporteServiceTest
[INFO] Running com.medigestion.service.ReservaCitaPrioritariaAutomatedTest
[INFO] Running com.medigestion.service.ReservaCitaPrioritariaAutomatedTest$EscenariosReglaNegocioRF04
2026-11-05 11:35:12.027 [main] INFO  com.medigestion.service.CitaService - [RF-05 Ticket Emitido] Cita confirmada con Ticket MG-JMNL-0511 para paciente 45892134 en consultorio C-101
2026-11-05 11:35:12.036 [main] WARN  com.medigestion.service.CitaService - [RF-04 Bloqueo Cita Duplicada] Regla de negocio: El paciente 45892134 ya cuenta con una cita activa para la especialidad Cardiología en la misma semana.
2026-11-05 11:35:12.046 [main] INFO  com.medigestion.service.CitaService - [RF-05 Ticket Emitido] Cita confirmada con Ticket MG-CRYL-0511 para paciente 45892134 en consultorio C-101
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.040 s -- in com.medigestion.service.ReservaCitaPrioritariaAutomatedTest$EscenariosReglaNegocioRF04
[INFO] Tests run: 0, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.046 s -- in com.medigestion.service.ReservaCitaPrioritariaAutomatedTest
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 20, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  12.669 s
```

---

## 7. Matriz de Trazabilidad Integral

Actualización formal de la trazabilidad desde el requerimiento funcional hasta la evidencia de testing:

| Requisito Funcional | Caso de Prueba | Clase / Método Automatizado | Tipo / Camino | Estado de Ejecución | Artefacto de Evidencia |
| :--- | :--- | :--- | :--- | :---: | :--- |
| **RF-04: 1 Cita Semanal** | `CP-MED-01` | `ReservaCitaPrioritariaAutomatedTest.cpMed01_DebeBloquearSegundaCitaMismaSemana` | Negativo / Lógica Negocio | **PASS** | Log WARN `[RF-04 Bloqueo Cita Duplicada]` + `Surefire Report` |
| **RF-04: Cita en Otra Semana** | `CP-MED-01` | `ReservaCitaPrioritariaAutomatedTest.cpMed01_DebePermitirCitaEnSemanaSiguiente` | Positivo / Límite de Rango | **PASS** | Log INFO `[RF-05 Ticket Emitido]` + Ticket `MG-XXXX-YY` |
| **RF-04: Cita Otra Especialidad** | `CP-MED-01` | `ReservaCitaPrioritariaAutomatedTest.cpMed01_DebePermitirCitaMismaSemanaDiferenteEspecialidad` | Positivo / Aislamiento Dominio | **PASS** | Confirmación estado `Reservado` en oferta |
| **RF-03 / RF-05: Concurrencia** | `CP-MED-02` | `CitaServiceTest.testReservarCita_Camino2_HorarioYaOcupado` | Negativo / Estado Cupo | **PASS** | `IllegalStateException` capturada |
| **RF-06 / RF-10: Regla 60 min** | `CP-MED-03` | `CitaServiceTest.testCancelarCita_Camino3_Regla60MinutosViolada` | Negativo / Límite Tiempo | **PASS** | Log WARN `[RF-06 Regla 60 Min]` |
| **RF-07: Demanda No Atendida** | `CP-MED-04` | `CitaServiceTest.testBuscarOfertasDisponibles_RegistraDemandaNoAtendida` | Positivo / Auditoría | **PASS** | Inserción en `DemandaNoAtendidaDAO` |

---

## 8. Conclusiones y Demostración

1. **Alineación con la Arquitectura:** La implementación de la prueba priorizada valida el cumplimiento estricto del desacoplamiento arquitectónico (DIP, Mockito) sin requerir base de datos física activa para certificar la lógica transaccional.
2. **Defensa ante Riesgos:** Las reglas críticas (RF-04, RF-03, RF-06) quedan blindadas contra regresiones de código mediante pruebas automatizadas repetibles y deterministas.
3. **Calidad y Transparencia:** La documentación detallada del defecto DEF-TEST-001 y su resolución mediante `mock-maker-subclass` garantiza la reproducibilidad de la suite en entornos de integración continua (CI/CD) modernos con Java 21+.
