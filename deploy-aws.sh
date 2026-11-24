#!/bin/bash
# ==============================================================================
# SCRIPT DE DESPLIEGUE AUTOMATIZADO EN AWS EC2 (HITO 80% - CURSO INTEGRADOR I)
# Proyecto: MediGestión - Sistema de Citas Médicas
# ==============================================================================

set -e

echo "=================================================================="
echo "  INICIANDO DESPLIEGUE EN AWS EC2 - MEDIGESTIÓN SPRING BOOT 3"
echo "=================================================================="

# 1. Verificar si Docker está instalado, si no, instalarlo
if ! [ -x "$(command -v docker)" ]; then
    echo "▶ Instalando Docker y dependencias en la instancia AWS EC2..."
    sudo apt-get update -y || sudo yum update -y
    sudo apt-get install -y docker.io docker-compose-v2 || sudo amazon-linux-extras install docker
    sudo systemctl start docker
    sudo systemctl enable docker
    sudo usermod -aG docker $USER
fi

# 2. Descargar o actualizar código fuente desde la rama oficial
echo "▶ Sincronizando rama oficial: feature/proyecto-final-spring-boot-thymeleaf..."
git fetch origin
git checkout feature/proyecto-final-spring-boot-thymeleaf
git pull origin feature/proyecto-final-spring-boot-thymeleaf

# 3. Levantar contenedores con Docker Compose
echo "▶ Construyendo y levantando contenedores (App Web + PostgreSQL)..."
docker compose down || true
docker compose up -d --build

# 4. Esperar arranque y verificar estado con Actuator
echo "▶ Esperando inicialización del servicio web..."
sleep 20

echo "▶ Verificando estado de salud del sistema mediante Spring Boot Actuator..."
curl -s http://localhost:8080/actuator/health || true

echo ""
echo "=================================================================="
echo "  ✔ DESPLIEGUE COMPLETADO EXITOSAMENTE EN AWS"
echo "  Acceso público: http://$(curl -s http://checkip.amazonaws.com):8080/"
echo "=================================================================="
