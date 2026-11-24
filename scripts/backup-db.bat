@echo off
REM ==============================================================================
REM Script de Respaldo Automatizado de Base de Datos para Windows - MediGestión
REM Hito 80%: Mantenimiento y Procedimientos
REM ==============================================================================

set DB_HOST=localhost
set DB_PORT=5432
set DB_NAME=medigestion_db
set DB_USER=postgres
set PGPASSWORD=postgres
set BACKUP_DIR=.\backups

if not exist "%BACKUP_DIR%" mkdir "%BACKUP_DIR%"

for /f "tokens=2-4 delims=/ " %%a in ('date /t') do (set mydate=%%c%%a%%b)
for /f "tokens=1-2 delims=/:" %%a in ("%TIME%") do (set mytime=%%a%%b)
set TIMESTAMP=%mydate%_%mytime%
set BACKUP_FILE=%BACKUP_DIR%\medigestion_backup_%TIMESTAMP%.sql

echo ==================================================================
echo [INICIO] Respaldando MediGestion DB a %BACKUP_FILE%...
echo ==================================================================

pg_dump -h %DB_HOST% -p %DB_PORT% -U %DB_USER% -d %DB_NAME% -F p -f "%BACKUP_FILE%"

if %ERRORLEVEL% equ 0 (
    echo [EXITO] Respaldo generado correctamente en %BACKUP_FILE%
) else (
    echo [ERROR] Ocurrio un fallo al exportar la base de datos con pg_dump.
)
