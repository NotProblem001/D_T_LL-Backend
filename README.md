# D_T_LL-Backend
Backend centralizado para la plataforma logística. Gestiona la lógica de negocio, la optimización de rutas y la persistencia de datos segura.

🏗️ Arquitectura y Stack
Lenguaje: Java 21 LTS

Framework: Spring Boot 3.2

Base de Datos: PostgreSQL + PostGIS (Geolocalización)

Optimización de Rutas: Jsprit (Motor VRP en Java)

Mapas/Ruteo: Conexión a instancia local de OSRM (Open Source Routing Machine)

Seguridad: Spring Security + JWT + Cifrado AES-256 para PII (Datos sensibles).

🧩 Módulos Principales
Driver API: Endpoints para la app móvil (Checklists, Ubicación GPS).

Admin API: Endpoints para el panel web (Carga Excel, Reportes).

Optimization Engine: Servicio que utiliza Jsprit para calcular el orden eficiente de paradas.

Excel Processor: Servicio asíncrono para leer/escribir archivos .xlsx grandes.

🚀 Ejecución con Docker (Recomendado)
El proyecto incluye un docker-compose.yml que levanta la Base de Datos y el servicio OSRM.bash

1. Levantar infraestructura (DB + OSRM)
docker-compose up -d

2. Ejecutar la aplicación Spring Boot
./mvnw spring-boot:run


## 📝 Configuración

Configura `src/main/resources/application.properties`:
```bash
properties
spring.datasource.url=jdbc:postgresql://localhost:5432/dondetellevo_db
spring.datasource.username=postgres
spring.datasource.password=secret
app.security.jwt-secret=tu_secreto_super_seguro_para_firmar_tokens
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
