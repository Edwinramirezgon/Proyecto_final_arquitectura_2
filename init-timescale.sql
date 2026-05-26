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

-- Datos semilla de sensores de Medellín
INSERT INTO sensors (sensor_id, name, zone_id, latitude, longitude, active, installed_at) VALUES
('SENSOR-MED-001', 'Sensor El Poblado', 'ZONA-MED-001', 6.2088, -75.5673, true, NOW()),
('SENSOR-MED-002', 'Sensor Laureles', 'ZONA-MED-002', 6.2447, -75.5907, true, NOW()),
('SENSOR-MED-003', 'Sensor Belén', 'ZONA-MED-003', 6.2308, -75.6111, true, NOW()),
('SENSOR-MED-004', 'Sensor Centro', 'ZONA-MED-004', 6.2476, -75.5658, true, NOW()),
('SENSOR-MED-005', 'Sensor Envigado', 'ZONA-MED-005', 6.1701, -75.5832, true, NOW()),
('SENSOR-MED-006', 'Sensor Itagüí', 'ZONA-MED-006', 6.1848, -75.5994, true, NOW()),
('SENSOR-MED-007', 'Sensor Bello', 'ZONA-MED-007', 6.3370, -75.5547, true, NOW()),
('SENSOR-MED-008', 'Sensor Sabaneta', 'ZONA-MED-008', 6.1513, -75.6169, true, NOW())
ON CONFLICT (sensor_id) DO NOTHING;
