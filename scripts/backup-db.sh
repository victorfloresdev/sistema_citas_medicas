#!/bin/bash
# ==============================================================================
# Script de Respaldo Automatizado de Base de Datos - MediGestión
# Hito 80%: Mantenimiento y Continuidad del Negocio (AWS / PostgreSQL)
# ==============================================================================
set -e

# Configuración de variables (con valores por defecto si no están en entorno)
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5432}"
DB_NAME="${DB_NAME:-medigestion_db}"
DB_USER="${DB_USER:-postgres}"
BACKUP_DIR="${BACKUP_DIR:-./backups}"
RETENTION_DAYS=7

TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
BACKUP_FILE="${BACKUP_DIR}/medigestion_backup_${TIMESTAMP}.sql.gz"
LOG_FILE="${BACKUP_DIR}/backup.log"

# Crear directorio de respaldo si no existe
mkdir -p "${BACKUP_DIR}"

echo "==================================================================" | tee -a "${LOG_FILE}"
echo "[$(date +"%Y-%m-%d %H:%M:%S")] Iniciando respaldo de MediGestión DB..." | tee -a "${LOG_FILE}"
echo "Host: ${DB_HOST}:${DB_PORT} | Base: ${DB_NAME} | Usuario: ${DB_USER}" | tee -a "${LOG_FILE}"

# Ejecutar pg_dump con compresión gzip al vuelo
if PGPASSWORD="${DB_PASSWORD:-postgres}" pg_dump -h "${DB_HOST}" -p "${DB_PORT}" -U "${DB_USER}" -d "${DB_NAME}" -F p | gzip > "${BACKUP_FILE}"; then
    BACKUP_SIZE=$(du -h "${BACKUP_FILE}" | cut -f1)
    echo "[$(date +"%Y-%m-%d %H:%M:%S")] ✔ Respaldo completado exitosamente: ${BACKUP_FILE} (${BACKUP_SIZE})" | tee -a "${LOG_FILE}"
else
    echo "[$(date +"%Y-%m-%d %H:%M:%S")] ❌ ERROR: Falló el respaldo de la base de datos." | tee -a "${LOG_FILE}"
    exit 1
fi

# Política de retención: Eliminar respaldos con más de 7 días de antigüedad
echo "[$(date +"%Y-%m-%d %H:%M:%S")] Aplicando política de retención (${RETENTION_DAYS} días)..." | tee -a "${LOG_FILE}"
find "${BACKUP_DIR}" -name "medigestion_backup_*.sql.gz" -type f -mtime +${RETENTION_DAYS} -exec rm -f {} \;
echo "[$(date +"%Y-%m-%d %H:%M:%S")] Limpieza de respaldos antiguos finalizada." | tee -a "${LOG_FILE}"
echo "==================================================================" | tee -a "${LOG_FILE}"
