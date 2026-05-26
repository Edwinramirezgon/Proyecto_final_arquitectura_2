# EcoMonitor — Red de Alerta Ambiental

Sistema de monitoreo de calidad del aire basado en **arquitectura hexagonal** y **microservicios**. Procesa lecturas de sensores de CO2, declara emergencias ambientales, notifica a la población suscrita por email e informa al operador sobre restricción vehicular recomendada en zonas críticas.

---

## Índice

1. [Contexto y problema](#contexto-y-problema)
2. [Reglas de negocio](#reglas-de-negocio)
3. [Arquitectura](#arquitectura)
4. [Servicios](#servicios)
5. [Tecnologías](#tecnologías)
6. [Infraestructura](#infraestructura)
7. [Observabilidad](#observabilidad)
8. [Manual de despliegue](#manual-de-despliegue)
9. [Variables de entorno](#variables-de-entorno)
10. [Accesos](#accesos)
11. [API Docs — Swagger](#api-docs--swagger)
12. [Decisiones técnicas](#decisiones-técnicas)

---

## Contexto y problema

El gobierno local ha desplegado sensores de CO2 en zonas urbanas. El sistema debe:

- Procesar miles de lecturas de sensores en tiempo real
- Detectar sensores fallidos con lecturas inconsistentes
- Declarar **Emergencia Ambiental** cuando el promedio supera el umbral
- Elevar la prioridad si el foco de contaminación está cerca de hospitales o escuelas
- Notificar masivamente a la población suscrita por email
- Enviar eventos de mantenimiento técnico para sensores fallidos

---

## Reglas de negocio

| # | Regla | Implementación |
|---|---|---|
| 0 | Solo se aceptan lecturas de sensores **registrados y activos** en la base de datos | `SensorReadingService` → `SensorRepository.findBySensorId()` |
| 1 | Una alerta de **Emergencia** solo se valida si el promedio de **≥3 sensores diferentes** en la misma zona supera los **150 µg/m³** | `SensorReadingService` — ventana de 5 minutos |
| 2 | Consulta **síncrona** al mapa de Zonas Sensibles para elevar prioridad si hay hospital o escuela cercana | `ZoneProxyController` → `GET /zones/nearest` |
| 3 | Si un sensor envía datos inconsistentes (salto > 200 µg/m³ en 1 segundo), el dato se **ignora** y se envía evento asíncrono de mantenimiento | `SensorReading.isInconsistentWith()` → `maintenance_queue` |

### Niveles de alerta

| Nivel | Condición |
|---|---|
| `CRITICAL` | Promedio ≥ 150 µg/m³ + zona sensible cercana (prioridad ≥ 8) |
| `HIGH` | Promedio ≥ 150 µg/m³ sin zona sensible cercana |
| `MEDIUM` | Promedio entre 100–149 µg/m³ |

---

## Arquitectura

### Diagrama C4 — Nivel 2 (Contenedores)

```
┌─────────────────────────────────────────────────────────────────────┐
│                        EcoMonitor System                            │
│                                                                     │
│  [React Frontend :3000]                                             │
│         │  HTTP (nginx proxy)                                       │
│         ▼                                                           │
│  [MS-PollutionAnalytics :8084]  ◄── TimescaleDB :5433              │
│         │                                                           │
│         ├── GET /zones/nearest ──► [MS-ZoneService :8082]          │
│         │   SÍNCRONO (REST)         └── PostgreSQL :5432            │
│         │                                                           │
│         ├── POST /auth/** ──────► [auth-service :8081]             │
│         │   SÍNCRONO (proxy)        └── PostgreSQL :5432            │
│         │                              └── Redis :6379 (tokens)    │
│         │                                                           │
│         ├──► alert_queue ──────► [MS-EmergencyAlert :8085]         │
│         │   ASÍNCRONO (RabbitMQ)    └── PostgreSQL :5432            │
│         │                                                           │
│         └──► maintenance_queue ─► [MS-EmergencyAlert :8085]        │
│             ASÍNCRONO (RabbitMQ)                                    │
│                                                                     │
│  [RabbitMQ :5672]  [Redis :6379]                                    │
└─────────────────────────────────────────────────────────────────────┘
```

### Comunicación síncrona vs asíncrona

| Comunicación | Tipo | Justificación |
|---|---|---|
| Frontend → pollution-analytics | Síncrono HTTP | El usuario espera la respuesta |
| pollution-analytics → zone-service | Síncrono HTTP | Necesita la respuesta para calcular el nivel de alerta |
| pollution-analytics → auth-service | Síncrono HTTP (proxy) | El usuario espera el token para continuar |
| pollution-analytics → alert_queue | Asíncrono RabbitMQ | No bloquea el procesamiento de nuevas lecturas |
| pollution-analytics → maintenance_queue | Asíncrono RabbitMQ | Notificación no crítica para el flujo principal |

### Patrón hexagonal (aplicado en todos los servicios)

```
src/main/java/com/demo/<servicio>/
├── domain/
│   ├── model/       ← Entidades puras, sin frameworks
│   └── exception/   ← Excepciones de dominio (RuntimeException)
├── application/
│   ├── port/in/     ← Interfaces de casos de uso
│   ├── port/out/    ← Interfaces de salida (repositorios, clientes)
│   └── usecase/     ← Implementación de lógica de negocio
└── infrastructure/
    ├── adapter/in/  ← Controllers REST, Listeners RabbitMQ
    ├── adapter/out/ ← JPA, RabbitMQ producers, HTTP clients
    └── config/      ← Configuración Spring, beans
```

---

## Servicios

### MS-PollutionAnalytics (puerto 8084) — Servicio principal
- Recibe lecturas de sensores de CO2
- Aplica validación de sensor + 3 reglas de negocio
- **CRUD de sensores** con persistencia en TimescaleDB
- Actúa como **API Gateway** para el frontend (proxy a auth y zone)
- **Circuit Breakers** con Resilience4j para auth-service y zone-service
- Base de datos: **TimescaleDB** (series de tiempo optimizadas)

### MS-ZoneService (puerto 8082)
- CRUD de zonas sensibles (hospitales, escuelas, parques)
- Endpoint de proximidad geográfica con fórmula Haversine
- Datos semilla: **13 zonas reales del Valle de Aburrá (Medellín)** — 5 hospitales, 5 escuelas/universidades, 3 parques
- Base de datos: PostgreSQL

### MS-EmergencyAlert (puerto 8085)
- Consumidor asíncrono de `alert_queue` y `maintenance_queue`
- Notifica al equipo técnico siempre
- Notifica a **suscriptores de la zona afectada** con email personalizado
- Base de datos: PostgreSQL (logs de notificaciones)

### auth-service (puerto 8081)
- Autenticación JWT con **access token** (15 min) y **refresh token** (7 días)
- Tokens almacenados en Redis — revocar = eliminar la clave
- Gestión de **suscripciones** de usuarios a zonas
- Base de datos: PostgreSQL + Redis

### frontend-ecomonitor (puerto 3000)
- React 18 + Vite + React-Leaflet + Recharts
- Comunicación **exclusiva** con pollution-analytics
- Mapa interactivo de alertas y zonas sensibles
- Datos reales de calidad del aire vía **Open-Meteo Air Quality** (modelo CAMS Copernicus)
- Panel de suscripciones por zona

---

## Tecnologías

| Categoría | Tecnología | Versión |
|---|---|---|
| Lenguaje backend | Java | 21 LTS |
| Framework backend | Spring Boot | 3.3.6 |
| Lenguaje frontend | JavaScript (React) | 18 |
| Build frontend | Vite | 5 |
| ORM | Spring Data JPA + Hibernate | — |
| Mensajería | RabbitMQ | 3.13 |
| BD series de tiempo | TimescaleDB | latest-pg16 |
| BD relacional | PostgreSQL | 16 |
| Caché / tokens | Redis | 7 |
| Resiliencia | Resilience4j | 2.2.0 |
| Trazabilidad | OpenTelemetry + Jaeger | 1.56 |
| Métricas | Micrometer + Prometheus | — |
| Logs | Loki + Promtail | 2.9.4 |
| Dashboards | Grafana | 10.4.0 |
| Mapas | Leaflet + OpenStreetMap | 1.9.4 |
| Datos reales | Open-Meteo Air Quality (CAMS) | — |
| Contenedores | Docker + Docker Compose | — |
| Servidor web | Nginx | alpine |

---

## Infraestructura

### Bases de datos

| Base de datos | Motor | Servicio |
|---|---|---|
| `authdb` | PostgreSQL :5434 | auth-service |
| `zonesdb` | PostgreSQL :5434 | zone-service |
| `alertdb` | PostgreSQL :5434 | emergency-alert |
| `pollutiondb` | TimescaleDB :5433 | pollution-analytics |

### Colas RabbitMQ

| Cola | Productor | Consumidor |
|---|---|---|
| `alert_queue` | pollution-analytics | emergency-alert |
| `maintenance_queue` | pollution-analytics | emergency-alert |
| `alert_queue.dlq` | RabbitMQ (DLQ automática) | intervención manual |
| `maintenance_queue.dlq` | RabbitMQ (DLQ automática) | intervención manual |

### Puertos expuestos

| Servicio | Puerto |
|---|---|
| Frontend | 3000 |
| pollution-analytics API | 8084 |
| PostgreSQL | 5434 |
| TimescaleDB | 5433 |
| Redis | 6380 |
| RabbitMQ AMQP | 5672 |
| RabbitMQ Management UI | 15672 |
| Grafana | 3001 |
| Prometheus | 9090 |
| Jaeger UI | 16686 |
| Loki | 3100 |

---

## Observabilidad

El sistema implementa los **3 pilares de observabilidad**:

### Pilar 1 — Métricas y Latencia (Prometheus + Grafana)
- Latencia P50 / P95 / P99 por servicio
- Requests por segundo y errores HTTP
- Estado de Circuit Breakers en tiempo real
- Lecturas de sensores procesadas
- Mensajes RabbitMQ publicados/consumidos

### Pilar 2 — Logs (Loki + Promtail + Grafana)
- Logs en JSON estructurado con `service`, `level`, `traceId`
- Panel de logs en vivo por cada microservicio
- Panel de ERRORs de todos los servicios
- Correlación directa log → traza (clic en traceId abre Jaeger)

### Pilar 3 — Consumo de recursos (Prometheus + Grafana)
- CPU usage por servicio
- JVM Heap y Non-Heap usados
- Threads activos y daemon
- Garbage Collection (tiempo y frecuencia)
- Conexiones de base de datos (HikariCP)
- Uptime por servicio

### Trazabilidad distribuida (Jaeger)
Cada request genera una traza que muestra el flujo completo:
```
POST /sensors/readings (pollution-analytics)
  └── GET /zones/nearest (zone-service)     ← síncrono
  └── publish → alert_queue (RabbitMQ)      ← asíncrono
```

---

## Manual de despliegue

### Requisitos previos

- **Docker** y **Docker Compose** instalados
- Cuenta de **Gmail** con App Password habilitado

### Pasos

**1. Clonar el repositorio**
```bash
git clone https://github.com/Edwinramirezgon/proyecto_integrador
cd Proyecto Integrador
```

**2. Configurar variables de entorno**
```bash
cp .env.example .env
```

Editar `.env`:
```env
POSTGRES_PASSWORD=postgres123
JWT_SECRET=ecomonitor-jwt-secret-key-2024!!
REDIS_PASSWORD=redis123
GMAIL_USERNAME=tu-correo@gmail.com
GMAIL_APP_PASSWORD=tu-app-password-de-gmail
```

> **Gmail App Password:** Ve a tu cuenta Google → Seguridad → Verificación en 2 pasos → Contraseñas de aplicaciones

**3. Levantar todos los servicios**
```bash
docker compose up -d --build
```

**4. Verificar que todos los servicios están corriendo**
```bash
docker compose ps
```

Todos los servicios deben estar en estado `running` o `healthy`.

**5. Verificar la inicialización de TimescaleDB**
```bash
docker compose logs pollution-analytics | grep TimescaleDB
```
Debe mostrar: `[TimescaleDB] Hypertable sensor_readings activa correctamente.`

### Comandos útiles

```bash
# Ver logs de un servicio
docker compose logs -f pollution-analytics

# Reconstruir un solo servicio
docker compose up -d --build pollution-analytics

# Detener todo
docker compose down

# Detener y eliminar volúmenes (reset completo)
docker compose down -v
```

---

## Variables de entorno

| Variable | Descripción | Requerida |
|---|---|---|
| `POSTGRES_PASSWORD` | Contraseña de PostgreSQL y TimescaleDB | ✅ |
| `JWT_SECRET` | Clave secreta para firmar tokens JWT | ✅ |
| `REDIS_PASSWORD` | Contraseña de Redis | ✅ |
| `GMAIL_USERNAME` | Correo Gmail para envío de alertas | ✅ |
| `GMAIL_APP_PASSWORD` | App Password de Gmail (no la contraseña normal) | ✅ |

---

## Accesos

| Servicio | URL | Credenciales |
|---|---|---|
| **Frontend EcoMonitor** | http://localhost:3000 | admin / Admin123! |
| **Grafana** (dashboards) | http://localhost:3001 | admin / admin |
| **Prometheus** (métricas) | http://localhost:9090 | — |
| **Jaeger** (trazas) | http://localhost:16686 | — |
| **RabbitMQ** (colas) | http://localhost:15672 | guest / guest |
| **Loki** (logs API) | http://localhost:3100 | — |

---

## API Docs — Swagger

Cada microservicio expone su propia documentación interactiva generada con **SpringDoc OpenAPI 2.3.0**. Disponible una vez levantado el stack con `docker compose up`.

| Servicio | Swagger UI | API Docs (JSON) |
|---|---|---|
| **pollution-analytics** | http://localhost:8084/swagger-ui.html | http://localhost:8084/api-docs |
| **zone-service** | http://localhost:8082/swagger-ui.html | http://localhost:8082/api-docs |
| **auth-service** | http://localhost:8081/swagger-ui.html | http://localhost:8081/api-docs |
| **emergency-alert** | http://localhost:8085/swagger-ui.html | http://localhost:8085/api-docs |

### Endpoints principales por servicio

**pollution-analytics** `:8084`
| Método | Endpoint | Descripción |
|---|---|---|
| `POST` | `/sensors/readings` | Ingestar lectura de sensor CO2 |
| `GET` | `/sensors/{sensorId}/readings` | Historial de lecturas por sensor |
| `POST` | `/sensors/management` | Registrar nuevo sensor |
| `GET` | `/sensors/management` | Listar todos los sensores |
| `GET` | `/sensors/management/active` | Listar sensores activos |
| `GET` | `/sensors/management/zone/{zoneId}` | Sensores por zona |
| `GET` | `/sensors/management/{sensorId}` | Obtener sensor por ID |
| `PUT` | `/sensors/management/{sensorId}` | Actualizar sensor |
| `POST` | `/sensors/management/{sensorId}/activate` | Activar sensor |
| `POST` | `/sensors/management/{sensorId}/deactivate` | Desactivar sensor |
| `DELETE` | `/sensors/management/{sensorId}` | Eliminar sensor |
| `GET` | `/alerts` | Todas las alertas |
| `GET` | `/alerts/active` | Alertas activas |
| `GET` | `/air-quality/colombia` | Datos reales PM2.5/AQI (Open-Meteo) |
| `POST` | `/auth/login` | Proxy → auth-service |
| `POST` | `/auth/register` | Proxy → auth-service |
| `POST` | `/auth/refresh` | Proxy → auth-service |
| `POST` | `/auth/logout` | Proxy → auth-service |
| `GET` | `/auth/validate` | Proxy → auth-service |
| `GET` | `/auth/me` | Proxy → auth-service |
| `POST` | `/auth/change-password` | Proxy → auth-service |
| `POST` | `/auth/forgot-password` | Proxy → auth-service |
| `POST` | `/auth/reset-password` | Proxy → auth-service |
| `GET` | `/zones` | Proxy → zone-service |
| `GET` | `/zones/{id}` | Proxy → zone-service |
| `POST` | `/zones` | Proxy → zone-service |
| `PUT` | `/zones/{id}` | Proxy → zone-service |
| `DELETE` | `/zones/{id}` | Proxy → zone-service |
| `GET` | `/zones/nearest` | Proxy → zone-service |
| `POST` | `/users/{username}/subscriptions` | Proxy → auth-service |
| `DELETE` | `/users/{username}/subscriptions/{zoneId}` | Proxy → auth-service |
| `GET` | `/users/{username}/subscriptions` | Proxy → auth-service |
| `GET` | `/notifications` | Proxy → emergency-alert |

**zone-service** `:8082`
| Método | Endpoint | Descripción |
|---|---|---|
| `GET` | `/zones` | Listar todas las zonas sensibles |
| `POST` | `/zones` | Crear zona sensible |
| `PUT` | `/zones/{id}` | Actualizar zona |
| `DELETE` | `/zones/{id}` | Eliminar zona |
| `GET` | `/zones/nearest` | Zona más cercana (Haversine) |

**auth-service** `:8081`
| Método | Endpoint | Descripción |
|---|---|---|
| `POST` | `/auth/register` | Registrar usuario |
| `POST` | `/auth/login` | Login — devuelve access + refresh token |
| `POST` | `/auth/refresh` | Renovar access token |
| `POST` | `/auth/logout` | Revocar tokens en Redis |
| `GET` | `/auth/validate` | Validar token JWT |
| `GET` | `/auth/me` | Obtener usuario actual |
| `POST` | `/auth/change-password` | Cambiar contraseña (requiere token) |
| `POST` | `/auth/forgot-password` | Solicitar reset de contraseña |
| `POST` | `/auth/reset-password` | Restablecer contraseña con token |
| `POST` | `/users/{username}/subscriptions` | Suscribir usuario a zona |
| `DELETE` | `/users/{username}/subscriptions/{zoneId}` | Desuscribir usuario de zona |
| `GET` | `/users/{username}/subscriptions` | Listar suscripciones del usuario |

**emergency-alert** `:8085`
| Método | Endpoint | Descripción |
|---|---|---|
| `GET` | `/notifications` | Historial de notificaciones enviadas |

---

## Decisiones técnicas

### ¿Por qué TimescaleDB para pollution-analytics?
`sensor_readings` es una tabla de series de tiempo — millones de filas con timestamp. TimescaleDB particiona automáticamente por tiempo (hypertable), haciendo las queries de ventana temporal (últimos 5 minutos por zona) hasta 100x más rápidas que PostgreSQL estándar.

### ¿Por qué PostgreSQL para los demás servicios y no SQL Server?
PostgreSQL es open source, sin licencia, consume ~50MB en Docker vs ~2GB de SQL Server Developer. No hay justificación de negocio para dos motores relacionales. La rúbrica pide persistencia políglota SQL + NoSQL — PostgreSQL cubre SQL, Redis cubre NoSQL.

### ¿Por qué RabbitMQ y no Kafka?
Kafka es ideal para replay de eventos y millones de mensajes/segundo. Para el volumen de este sistema (alertas ambientales por zona), RabbitMQ es suficiente y más simple de operar. Kafka requeriría Zookeeper o KRaft, añadiendo ~600MB de imagen sin beneficio real.

### ¿Por qué el frontend solo habla con pollution-analytics?
Punto único de entrada — simplifica CORS, centraliza autenticación y evita que el frontend conozca la topología interna. pollution-analytics actúa como API Gateway ligero. El trade-off es que asume responsabilidad de proxy, pero es preferible a introducir Kong o Spring Cloud Gateway para el alcance del proyecto.

### ¿Por qué Circuit Breaker solo en pollution-analytics?
Es el único servicio que hace llamadas síncronas a otros servicios (auth-service y zone-service). Si zone-service cae, el fallback devuelve `Optional.empty()` y la alerta se genera como HIGH en lugar de CRITICAL — el sistema no se detiene. Si auth-service cae, el usuario recibe un mensaje claro y puede reintentar en 10 segundos.

### ¿Por qué Open-Meteo Air Quality para datos de calidad del aire?
Open-Meteo usa el modelo atmosférico CAMS (Copernicus Atmosphere Monitoring Service) de la ESA para generar datos de PM2.5 y AQI en tiempo real con actualización horaria. Es completamente gratuito, sin API key, sin registro, y permite consultar múltiples ubicaciones en una sola request. Se usan las coordenadas exactas de las 16 estaciones de la red AMVA del Valle de Aburrá para obtener datos representativos de cada municipio.

### ¿Por qué suscripciones en auth-service y no en pollution-analytics?
Las suscripciones son datos de usuario — pertenecen al dominio de autenticación/identidad. Pollution-analytics no debe conocer qué usuarios existen. La separación de responsabilidades es más limpia así, aunque añade una llamada HTTP desde emergency-alert a auth-service.
