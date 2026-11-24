@echo off
chcp 65001 > nul
title MediGestión - Sistema de Citas Médicas (Spring Boot 3 + Thymeleaf)
cls
echo ==================================================================
echo           MEDIGESTIÓN 80% - SISTEMA DE CITAS MÉDICAS
echo        Spring Boot 3 + Thymeleaf + AWS Ready (Java 21 LTS)
echo ==================================================================
echo.
echo  Iniciando servidor web embebido en http://localhost:8080/ ...
echo.
start "" http://localhost:8080/
java -jar "%~dp0target\medigestion-citas-1.0-SNAPSHOT.jar"
pause
