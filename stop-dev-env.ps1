# Script para detener el entorno de desarrollo
# Detiene los contenedores de Docker y mata los procesos de Java asociados

Write-Host "Deteniendo Base de Datos (MongoDB)..." -ForegroundColor Yellow
docker-compose down

Write-Host "Para detener los microservicios, tienes dos opciones:" -ForegroundColor Cyan
Write-Host "1. Cerrar manualmente las ventanas de PowerShell que se abrieron."
Write-Host "2. Ejecutar el siguiente comando para forzar el cierre de TODOS los procesos Java (Cuidado si tienes otras apps Java):"
Write-Host "   Stop-Process -Name java -Force -ErrorAction SilentlyContinue" -ForegroundColor Red

# Descomenta la siguiente línea si quieres que el script cierre todo automáticamente (Riesgoso)
Stop-Process -Name java -Force -ErrorAction SilentlyContinue

Write-Host "Entorno detenido (Docker)." -ForegroundColor Green
