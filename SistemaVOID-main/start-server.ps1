# Script para iniciar el servidor de desarrollo Spring Boot (con Hot-Reload / DevTools y base de datos H2)
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "  Iniciando Servidor Spring Boot (VOID)  " -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan

# Asegurar Java 21
$java21Path = "C:\Users\david\.vscode\extensions\redhat.java-1.54.0-win32-x64\jre\21.0.10-win32-x86_64"
if (Test-Path $java21Path) {
    $env:JAVA_HOME = $java21Path
    $env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
    Write-Host "[OK] Usando Java 21: $env:JAVA_HOME" -ForegroundColor Green
}

Write-Host "Iniciando aplicacion en puerto 8080..." -ForegroundColor Yellow
Write-Host "- Directorio de usuarios: http://localhost:8080/users" -ForegroundColor Cyan
Write-Host "- Consola H2 Database:   http://localhost:8080/h2-console" -ForegroundColor Cyan
Write-Host "- Autenticacion API:     http://localhost:8080/api/auth/login" -ForegroundColor Cyan
Write-Host ""
Write-Host "Credenciales de prueba precargadas:" -ForegroundColor Yellow
Write-Host "  Admin: admin@voidapp.com / admin123" -ForegroundColor White
Write-Host "  User:  demo@voidapp.com  / demo123" -ForegroundColor White
Write-Host "==========================================" -ForegroundColor Cyan

mvn spring-boot:run -f void-spring/pom.xml
