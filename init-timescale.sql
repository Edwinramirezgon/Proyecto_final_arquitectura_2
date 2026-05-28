-- Activar extensión TimescaleDB en pollutiondb
\c pollutiondb

CREATE EXTENSION IF NOT EXISTS timescaledb;

-- Tabla de sensores
CREATE TABLE IF NOT EXISTS sensors (
    id              bigserial       NOT NULL,
    sensor_id       varchar(100)    NOT NULL UNIQUE,
    name            varchar(255)    NOT NULL,
    zone_id         varchar(100)    NOT NULL,
    latitude        float8          NOT NULL,
    longitude       float8          NOT NULL,
    active          boolean         NOT NULL DEFAULT true,
    installed_at    timestamp(6)    NOT NULL,
    last_reading_at timestamp(6),
    PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_sensors_zone_id ON sensors(zone_id);
CREATE INDEX IF NOT EXISTS idx_sensors_active ON sensors(active);

-- Tabla de series de tiempo con PK compuesta requerida por TimescaleDB
CREATE TABLE IF NOT EXISTS sensor_readings (
    id          bigserial     NOT NULL,
    recorded_at timestamp(6)  NOT NULL,
    sensor_id   varchar(255)  NOT NULL,
    zone_id     varchar(255)  NOT NULL,
    latitude    float8        NOT NULL,
    longitude   float8        NOT NULL,
    co2_level   float8        NOT NULL,
    PRIMARY KEY (id, recorded_at)
);

SELECT create_hypertable('sensor_readings', 'recorded_at', if_not_exists => TRUE);

-- Tabla de alertas (PK simple, no es hypertable)
CREATE TABLE IF NOT EXISTS alerts (
    id                 bigserial     NOT NULL,
    active             boolean       NOT NULL,
    average_co2        float8        NOT NULL,
    latitude           float8        NOT NULL,
    level              varchar(255)  NOT NULL CHECK (level IN ('CRITICAL','HIGH','MEDIUM')),
    longitude          float8        NOT NULL,
    nearest_zone_name  varchar(255),
    triggered_at       timestamp(6)  NOT NULL,
    zone_id            varchar(255)  NOT NULL,
    PRIMARY KEY (id)
);

-- Datos semilla de sensores de Medellín (3 sensores por zona)
INSERT INTO sensors (sensor_id, name, zone_id, latitude, longitude, active, installed_at) VALUES
-- ZONA-MED-001 El Poblado
('SENSOR-MED-001',   'Sensor El Poblado A',  'ZONA-MED-001', 6.2088, -75.5673, true, NOW()),
('SENSOR-MED-001-B', 'Sensor El Poblado B',  'ZONA-MED-001', 6.2095, -75.5680, true, NOW()),
('SENSOR-MED-001-C', 'Sensor El Poblado C',  'ZONA-MED-001', 6.2081, -75.5665, true, NOW()),
-- ZONA-MED-002 Laureles
('SENSOR-MED-002',   'Sensor Laureles A',    'ZONA-MED-002', 6.2447, -75.5907, true, NOW()),
('SENSOR-MED-002-B', 'Sensor Laureles B',    'ZONA-MED-002', 6.2455, -75.5915, true, NOW()),
('SENSOR-MED-002-C', 'Sensor Laureles C',    'ZONA-MED-002', 6.2439, -75.5899, true, NOW()),
-- ZONA-MED-003 Belén
('SENSOR-MED-003',   'Sensor Belén A',       'ZONA-MED-003', 6.2308, -75.6111, true, NOW()),
('SENSOR-MED-003-B', 'Sensor Belén B',       'ZONA-MED-003', 6.2316, -75.6120, true, NOW()),
('SENSOR-MED-003-C', 'Sensor Belén C',       'ZONA-MED-003', 6.2300, -75.6102, true, NOW()),
-- ZONA-MED-004 Centro
('SENSOR-MED-004',   'Sensor Centro A',      'ZONA-MED-004', 6.2476, -75.5658, true, NOW()),
('SENSOR-MED-004-B', 'Sensor Centro B',      'ZONA-MED-004', 6.2484, -75.5666, true, NOW()),
('SENSOR-MED-004-C', 'Sensor Centro C',      'ZONA-MED-004', 6.2468, -75.5650, true, NOW()),
-- ZONA-MED-005 Envigado
('SENSOR-MED-005',   'Sensor Envigado A',    'ZONA-MED-005', 6.1701, -75.5832, true, NOW()),
('SENSOR-MED-005-B', 'Sensor Envigado B',    'ZONA-MED-005', 6.1709, -75.5840, true, NOW()),
('SENSOR-MED-005-C', 'Sensor Envigado C',    'ZONA-MED-005', 6.1693, -75.5824, true, NOW()),
-- ZONA-MED-006 Itagüí
('SENSOR-MED-006',   'Sensor Itagüí A',      'ZONA-MED-006', 6.1848, -75.5994, true, NOW()),
('SENSOR-MED-006-B', 'Sensor Itagüí B',      'ZONA-MED-006', 6.1856, -75.6002, true, NOW()),
('SENSOR-MED-006-C', 'Sensor Itagüí C',      'ZONA-MED-006', 6.1840, -75.5986, true, NOW()),
-- ZONA-MED-007 Bello
('SENSOR-MED-007',   'Sensor Bello A',       'ZONA-MED-007', 6.3370, -75.5547, true, NOW()),
('SENSOR-MED-007-B', 'Sensor Bello B',       'ZONA-MED-007', 6.3378, -75.5555, true, NOW()),
('SENSOR-MED-007-C', 'Sensor Bello C',       'ZONA-MED-007', 6.3362, -75.5539, true, NOW()),
-- ZONA-MED-008 Sabaneta
('SENSOR-MED-008',   'Sensor Sabaneta A',    'ZONA-MED-008', 6.1513, -75.6169, true, NOW()),
('SENSOR-MED-008-B', 'Sensor Sabaneta B',    'ZONA-MED-008', 6.1521, -75.6177, true, NOW()),
('SENSOR-MED-008-C', 'Sensor Sabaneta C',    'ZONA-MED-008', 6.1505, -75.6161, true, NOW())
-- ZONA-HOSPITAL-001 Hospital General de Medellín (prioridad 10 → alerta CRITICAL)
-- Coordenadas exactas del hospital para que /zones/nearest lo detecte dentro del radio 2km
('SENSOR-HOSP-001',   'Sensor Hospital General A', 'ZONA-HOSPITAL-001', 6.2526, -75.5696, true, NOW()),
('SENSOR-HOSP-001-B', 'Sensor Hospital General B', 'ZONA-HOSPITAL-001', 6.2528, -75.5698, true, NOW()),
('SENSOR-HOSP-001-C', 'Sensor Hospital General C', 'ZONA-HOSPITAL-001', 6.2524, -75.5693, true, NOW())
ON CONFLICT (sensor_id) DO NOTHING;
