@echo off
chcp 65001 > nul
title MediGestion - Sistema de Citas Medicas
cls
echo ==================================================================
echo           MEDIGESTION - SISTEMA DE CITAS MEDICAS
echo ==================================================================
echo.
java -jar "%~dp0target\medigestion-app.jar"
pause
