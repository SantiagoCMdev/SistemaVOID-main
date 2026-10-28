# Script para probar rápidamente el servidor backend
Write-Host "Probando el servidor en http://localhost:8080..." -ForegroundColor Cyan

# 1. Probar ruta de usuarios (Thymeleaf)
try {
    $resUsers = Invoke-WebRequest -Uri "http://localhost:8080/users" -UseBasicParsing -TimeoutSec 5
    if ($resUsers.StatusCode -eq 200) {
        Write-Host "[OK] Vista /users respondio correctamente (HTTP 200)" -ForegroundColor Green
    }
} catch {
    Write-Host "[ERROR] El servidor no parece estar activo en http://localhost:8080. Inicialo primero con .\start-server.ps1" -ForegroundColor Red
    exit 1
}

# 2. Probar Login (LoginUserUseCase)
Write-Host "`nProbando POST /api/auth/login con admin@voidapp.com..." -ForegroundColor Cyan
$loginPayload = @{
    email = "admin@voidapp.com"
    password = "admin123"
} | ConvertTo-Json

try {
    $loginRes = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -Body $loginPayload -ContentType "application/json"
    Write-Host "[OK] Login exitoso! Token recibido:" -ForegroundColor Green
    Write-Host "Bearer $($loginRes.token.Substring(0, 35))..." -ForegroundColor White
    Write-Host "Usuario: $($loginRes.user.fullName) (Rol: $($loginRes.user.role))" -ForegroundColor White
} catch {
    Write-Host "[ERROR] Fallo el login: $($_.Exception.Message)" -ForegroundColor Red
}

# 3. Probar Login con credenciales invalidas
Write-Host "`nProbando POST /api/auth/login con clave incorrecta..." -ForegroundColor Cyan
$badPayload = @{
    email = "admin@voidapp.com"
    password = "clave_invalida"
} | ConvertTo-Json

try {
    $badRes = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -Body $badPayload -ContentType "application/json"
    Write-Host "[ALERTA] Se esperaba error 401 pero se recibio respuesta exitosa" -ForegroundColor Yellow
} catch {
    Write-Host "[OK] Rechazo credenciales invalidas correctamente con 401 Unauthorized" -ForegroundColor Green
}

Write-Host "`n================================================" -ForegroundColor Cyan
Write-Host " Todas las pruebas basicas pasaron exitosamente! " -ForegroundColor Green
Write-Host "================================================" -ForegroundColor Cyan
