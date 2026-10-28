@echo off
title Sistema VOID - Fullstack Launcher
echo ========================================================
echo   Iniciando Backend (Spring Boot) y Frontend (Next.js)
echo ========================================================

echo Abriendo Servidor Backend en nueva ventana...
start "Backend Spring Boot" cmd /k "call start-server.bat"

echo Abriendo Servidor Frontend en nueva ventana...
start "Frontend Next.js" cmd /k "call start-frontend.bat"

echo.
echo Ambos servidores han sido iniciados en ventanas separadas:
echo - Backend API:  http://localhost:8080
echo - Frontend Web: http://localhost:3000
echo - H2 Console:   http://localhost:8080/h2-console
echo ========================================================
