# Manual de Procedimientos: Mantenimiento y Respaldos Automatizados
## MediGestión - Sistema Integral de Citas Médicas
**Curso:** Curso Integrador I: Sistemas - Software (UTP)  
**Hito:** Avance al 80% (Semana 15)  
**Tecnologías:** Spring Boot 3 `@Scheduled`, PostgreSQL 16, Bash Scripts, Docker, AWS EC2

---

## 1. Introducción y Alcance Operativo

El presente documento describe los procedimientos técnicos para garantizar la continuidad operativa, integridad de la base de datos y tareas de mantenimiento preventivo del sistema **MediGestión**.

El plan cubre:
1. **Tareas Programadas en Código (Cron Jobs):** Ejecutadas nativamente por el motor de Spring Boot mediante `@EnableScheduling` y `@Scheduled`.
2. **Procedimientos de Respaldo Automatizado (Backups):** Generación de copias de seguridad de PostgreSQL con compresión y políticas de retención.
3. **Procedimiento de Recuperación ante Desastres (Disaster Recovery):** Restauración íntegra de los datos.
4. **Guía de Automatización en Servidores:** Configuración en Crontab de Linux (AWS EC2) y en el Programador de Tareas de Windows.

---

## 2. Tareas Programadas Nativas del Sistema (Cron Jobs)

Las tareas de mantenimiento del negocio hospitalario están centralizadas en la clase `com.medigestion.task.MantenimientoScheduledTask`.

### 2.1 Catálogo de Cron Jobs Implementados

| Nombre de la Tarea | Expresión Cron | Frecuencia | Responsabilidad / Acción |
| :--- | :--- | :--- | :--- |
| **Auditoría Horaria de Citas** | `0 0 * * * ?` | Cada 1 hora en punto | - Verifica el volumen de citas y demanda no atendida en espera.<br>- Registra advertencias operativas si la demanda insatisfecha supera 15 pacientes.<br>- Inserta un registro de auditoría en la tabla `bitacora_evento`. |
| **Consolidación Diaria Nocturna** | `0 0 2 * * ?` | Todos los días a las 02:00 AM | - Cierre y consolidación del estado de las citas finalizadas.<br>- Verificación de coherencia relacional entre citas y médicos.<br>- Registro formal de mantenimiento nocturno en bitácora. |

### 2.2 Habilitación en la Arquitectura
* Anotación `@EnableScheduling` en la clase principal `MediGestionApplication.java`.
* Inyección de dependencias segura mediante `@Autowired` hacia `ReservaCitaDAO`, `DemandaNoAtendidaDAO` y `BitacoraEventoDAO`.
* Trazabilidad con **MDC** (`CRON-HORARIO-...` y `CRON-NOCTURNO-...`) para aislar los logs de los cron jobs en `logs/medigestion.log`.

---

## 3. Procedimientos de Respaldo (Backup)

Los respaldos se generan mediante scripts funcionales que extraen la base de datos PostgreSQL, la comprimen en formato `.sql.gz` y aplican políticas de rotación histórica.

### 3.1 Script de Respaldo (`scripts/backup-db.sh`)

```bash
# Variables de entorno opcionales:
# DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD, BACKUP_DIR

# Ejecución manual o por tarea programada:
./scripts/backup-db.sh
```

**Características Técnicas:**
* **Compresión al vuelo:** Utiliza `gzip` para reducir el tamaño del volcado en hasta un $85\%$.
* **Nomenclatura estandarizada:** `medigestion_backup_YYYYMMDD_HHMMSS.sql.gz`.
* **Registro de actividad:** Bitácora histórica en `backups/backup.log`.
* **Política de Retención Automática:** Elimina copias con más de 7 días de antigüedad (`find ... -mtime +7 -delete`) para proteger el espacio en disco de AWS EC2.

### 3.2 Programación en Servidores de Producción (AWS EC2 / Linux)

Para automatizar la ejecución diaria del respaldo a las **03:00 AM** (una hora después del cron job nocturno), se configura en el **crontab** del servidor:

```bash
# Abrir editor de tareas de cron del usuario
crontab -e

# Agregar la siguiente línea al final del archivo:
0 3 * * * /opt/medigestion/scripts/backup-db.sh >> /var/log/medigestion-backup-cron.log 2>&1
```

---

## 4. Procedimiento de Recuperación ante Desastres (Disaster Recovery)

Ante una eventualidad crítica (corrupción de almacenamiento o fallo de servidor), la restauración se efectúa con el script funcional `scripts/restore-db.sh`.

### 4.1 Pasos para la Restauración

1. **Identificar la copia de seguridad objetivo:**
   ```bash
   ls -la backups/
   # Ejemplo seleccionado: backups/medigestion_backup_20261124_030000.sql.gz
   ```

2. **Detener temporalmente el contenedor de la aplicación:**
   ```bash
   docker compose stop medigestion-app
   ```

3. **Ejecutar el script de restauración:**
   ```bash
   chmod +x ./scripts/restore-db.sh
   ./scripts/restore-db.sh ./backups/medigestion_backup_20261124_030000.sql.gz
   ```

4. **Reiniciar el contenedor de la aplicación y verificar salud:**
   ```bash
   docker compose start medigestion-app
   curl -s http://localhost:8080/actuator/health
   ```

---

## 5. Matriz de Validación de Procedimientos

| Procedimiento | Método de Validación | Resultado Esperado |
| :--- | :--- | :--- |
| **Cron Job Horario** | Inspeccionar `logs/medigestion.log` con patrón `[CRON-JOB]` | Evento registrado con total de citas y estado normal. |
| **Generación de Backup** | Ejecutar `./scripts/backup-db.sh` o `backup-db.bat` | Archivo `.sql.gz` o `.sql` generado en `backups/` con tamaño $> 0$ bytes. |
| **Purga de Retención** | Modificar fecha de un backup ficticio a $+8$ días | El script elimina el archivo expirado sin tocar los vigentes. |
| **Restauración DB** | Ejecutar `./scripts/restore-db.sh` | Esquema y datos íntegros restaurados; `/actuator/health` retorna `{"status":"UP"}`. |
