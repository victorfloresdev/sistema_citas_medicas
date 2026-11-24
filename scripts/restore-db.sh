#!/bin/bash
# ==============================================================================
# Script de Restauración de Base de Datos - MediGestión
# Hito 80%: Recuperación ante Desastres (Disaster Recovery - AWS / PostgreSQL)
# Uso: ./scripts/restore-db.sh <ruta_al_archivo_backup.sql.gz>
# ==============================================================================
set -e

if [ -z "$1" ]; then
    echo "Uso: $0 <archivo_backup.sql.gz>"
    echo "Ejemplo: $0 ./backups/medigestion_backup_20261124_020000.sql.gz"
    exit 1
fi

BACKUP_FILE="$1"

if [ ! -f "${BACKUP_FILE}" ]; then
    echo "ERROR: El archivo de respaldo '${BACKUP_FILE}' no existe."
    exit 1
fi

DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5432}"
DB_NAME="${DB_NAME:-medigestion_db}"
DB_USER="${DB_USER:-postgres}"

echo "=================================================================="
echo "⚠️  ADVERTENCIA: Esta operación restaurará la base de datos ${DB_NAME}."
echo "Archivo: ${BACKUP_FILE}"
echo "Destino: ${DB_HOST}:${DB_PORT} / ${DB_NAME}"
echo "=================================================================="

# Descomprimir y restaurar en PostgreSQL
echo "[$(date +"%Y-%m-%d %H:%M:%S")] Restaurando datos en PostgreSQL..."
if gunzip -c "${BACKUP_FILE}" | PGPASSWORD="${DB_PASSWORD:-postgres}" psql -h "${DB_HOST}" -p "${DB_PORT}" -U "${DB_USER}" -d "${DB_NAME}"; then
    echo "[$(date +"%Y-%m-%d %H:%M:%S")] ✔ Restauración completada exitosamente."
else
    echo "[$(date +"%Y-%m-%d %H:%M:%S")] ❌ ERROR: Falló el proceso de restauración."
    exit 1
fi
