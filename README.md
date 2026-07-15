# D_T_LL-Backend
Backend centralizado para la plataforma logística. Gestiona la lógica de negocio, la optimización de rutas y la persistencia de datos segura.

🏗️ Arquitectura y Stack
Lenguaje: Java 17 LTS

Framework: Spring Boot 3.2

Base de Datos: PostgreSQL + PostGIS (Geolocalización), migraciones versionadas con Flyway

Optimización de Rutas: Jsprit (Motor VRP en Java)

Mapas/Ruteo: Conexión a instancia local de OSRM (Open Source Routing Machine)

Seguridad: Spring Security + JWT (roles ADMIN / EMPRESA / PASAJERO / CONDUCTOR)

🧩 Módulos Principales
Auth: Login local, Google, LinkedIn (pasajero/empresa/admin) y RUT+PIN (conductor).

Driver API: Endpoints para la app móvil (Checklists, Ubicación GPS).

Admin API: Endpoints para el panel web (Carga Excel, Reportes).

Optimization Engine: Servicio que utiliza Jsprit + OSRM para calcular el orden eficiente de paradas.

Excel Processor: Servicio asíncrono para leer/escribir archivos .xlsx grandes.

🚀 Ejecución con Docker (Recomendado)
El proyecto incluye un `docker-compose.yml` que levanta Postgres+PostGIS y OSRM.

1. Preprocesar el extracto de OSRM (una sola vez, antes del primer `docker-compose up`):
   ```bash
   mkdir -p osrm-data && cd osrm-data
   curl -O https://download.geofabrik.de/south-america/chile-latest.osm.pbf
   docker run -t -v "$(pwd):/data" osrm/osrm-backend osrm-extract -p /opt/car.lua /data/chile-latest.osm.pbf
   docker run -t -v "$(pwd):/data" osrm/osrm-backend osrm-partition /data/chile-latest.osrm
   docker run -t -v "$(pwd):/data" osrm/osrm-backend osrm-customize /data/chile-latest.osrm
   ```
2. Levantar infraestructura (DB + OSRM):
   ```bash
   docker-compose up -d
   ```
3. Ejecutar la aplicación Spring Boot (las migraciones Flyway corren automáticamente al iniciar):
   ```bash
   mvn spring-boot:run
   ```

## 📝 Configuración

Toda la configuración se define vía variables de entorno (ver `src/main/resources/application.yml`), con valores por defecto para desarrollo local:

```bash
# Opción A: variables sueltas (las usa render.yaml, construye el JDBC URL automáticamente)
DB_HOST=localhost
DB_PORT=5432
DB_NAME=dtll_db
DB_USER=postgres
DB_PASSWORD=password
# Opción B: JDBC URL completo, tiene prioridad sobre las variables sueltas si se define
# (útil con proveedores externos como Neon/Supabase que no exponen host/puerto por separado)
# DATABASE_URL=jdbc:postgresql://host:5432/db?sslmode=require
JWT_SECRET=tu_secreto_super_seguro_para_firmar_tokens
ALLOWED_ORIGINS=http://localhost:5173,http://localhost:5174,http://localhost:5175
OSRM_URL=http://localhost:5000
GOOGLE_CLIENT_ID=
LINKEDIN_CLIENT_ID=
LINKEDIN_CLIENT_SECRET=
LINKEDIN_REDIRECT_URI=
ADMIN_BOOTSTRAP_EMAIL=admin@dondetellevo.cl
ADMIN_BOOTSTRAP_PASSWORD=cambia-esta-clave
```

### 4. Repositorio: `D_T_LL Ride-Hailing App`
**Descripción:** Aplicación móvil para los conductores.
**Enfoque:** Funcionamiento offline, bajo consumo de batería y navegación nativa.

# 📱 Donde Te Llevo - App Conductor (KMP)

Aplicación móvil multiplataforma (Android/iOS) desarrollada con **Kotlin Multiplatform (KMP)** y **Compose Multiplatform**. Diseñada para los conductores de la flota.

## 🚀 Tecnologías

*   **Lenguaje:** Kotlin (Lógica compartida al 90%)
*   **UI:** Jetpack Compose (Android) / Compose Multiplatform (iOS - Experimental/Alpha)
*   **Mapas:** Google Maps SDK (Nativo Android / Interop iOS)
*   **Navegación Externa:** Intents para Waze/Google Maps
*   **Base de Datos Local:** SQLDelight o Room (Soporte Offline)
*   **Networking:** Ktor Client

## 📱 Funcionalidades

*   **Login con Código:** Ingreso rápido mediante `route_code`.
*   **Checklist de Pasajeros:** Marcado de asistencia (Subió/No Show/Cuenta Propia).
*   **Navegación:** Botón directo para iniciar ruta en GPS externo.
*   **Modo Offline:** Sincronización automática cuando recupera conexión.

## 🛠️ Configuración del Entorno

1.  **Requisitos:**
    *   Android Studio Koala o superior.
    *   JDK 17.
    *   Xcode (para compilar la versión de iOS).

2.  **Claves de API:**
    Crea un archivo `local.properties` en la raíz (no lo subas a git):properties
    MAPS_API_KEY=tu_google_maps_key
    ```

3.  **Compilar:**
    *   **Android:** Selecciona la configuración `androidApp` y presiona Run.
    *   **iOS:** Abre `iosApp/iosApp.xcworkspace` en Xcode o ejecuta desde Android Studio con el plugin KMM.

## 📂 Estructura del Proyecto

*   `commonMain`: Código compartido (Modelos, Red, Lógica de Negocio).
*   `androidMain`: Implementaciones específicas de Android (Servicios de ubicación, Permisos).
*   `iosMain`: Implementaciones específicas de iOS.
