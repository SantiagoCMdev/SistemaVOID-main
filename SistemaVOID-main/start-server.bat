@echo off
title Servidor Spring Boot - Sistema VOID
echo ==========================================
echo   Iniciando Servidor Spring Boot (VOID)
echo ==========================================

set JAVA21_PATH=C:\Users\david\.vscode\extensions\redhat.java-1.54.0-win32-x64\jre\21.0.10-win32-x86_64
if exist "%JAVA21_PATH%" (
    set "JAVA_HOME=%JAVA21_PATH%"
    set "PATH=%JAVA_HOME%\bin;%PATH%"
    echo [OK] Usando Java 21: %JAVA_HOME%
)

echo.
echo URLs disponibles:
echo - Directorio de usuarios: http://localhost:8080/users
echo - Consola H2 Database:   http://localhost:8080/h2-console
echo - Autenticacion API:     http://localhost:8080/api/auth/login
echo.
echo Credenciales de prueba:
echo   Admin: admin@voidapp.com / admin123
echo   User:  demo@voidapp.com  / demo123
echo ==========================================
echo.

mvn spring-boot:run -f void-spring/pom.xml
pause
