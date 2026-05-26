package com.demo.pollution.domain.model;

public enum AlertLevel {
    CRITICAL,   // emergencia + zona sensible (hospital/escuela)
    HIGH,       // emergencia sin zona sensible cercana
    MEDIUM      // promedio entre 100-150 µg/m³ — advertencia
}
