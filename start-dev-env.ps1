# Script para iniciar el entorno de desarrollo Backend
# Lanza los servicios necesarios para la integración con la Landing Page

Write-Host "Iniciando Base de Datos (MongoDB)..." -ForegroundColor Green
docker-compose up -d mongo

# Función para iniciar un servicio en una nueva pestaña/ventana
function Start-Service {
    param (
        [string]$ServiceName,
        [string]$Path
    )
    Write-Host "Lanzando $ServiceName..." -ForegroundColor Cyan
    # Abre una nueva ventana de PowerShell y ejecuta el servicio
    Start-Process powershell -ArgumentList "-Command", "cd $Path; mvn spring-boot:run 2>&1 | Tee-Object -FilePath $Path\auth.log"
    Start-Sleep -Seconds 5 # Espera un poco entre servicios para evitar sobrecarga inicial
}

$Root = Get-Location

# 1. Infraestructura
Start-Service -ServiceName "Eureka Server" -Path "$Root\Eureka-Server"
Start-Service -ServiceName "Config Server" -Path "$Root\Config-Server"
Start-Sleep -Seconds 10 # Esperar a que Eureka y Config arranquen bien

# 2. Gateway
# 2. Gateway
Start-Process powershell -ArgumentList "-Command", "cd $Root\Api-Gateway; mvn spring-boot:run 2>&1 | Tee-Object -FilePath $Root\Api-Gateway\gateway.log"

# 3. Servicios Core (Landing Page)
Start-Service -ServiceName "Auth Service" -Path "$Root\Auth-Service"
Start-Service -ServiceName "Report Service" -Path "$Root\Report-Service"
Start-Service -ServiceName "Booking Service" -Path "$Root\Booking-Service"

Write-Host "Todos los servicios han sido lanzados." -ForegroundColor Green
Write-Host "Esperar unos minutos para que todo esté completamente operativo."

# .\start-dev-env.ps1 
# .\stop-dev-env.ps1
