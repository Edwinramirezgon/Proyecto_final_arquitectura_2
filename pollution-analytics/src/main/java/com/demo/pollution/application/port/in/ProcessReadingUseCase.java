package com.demo.pollution.application.port.in;

import com.demo.pollution.domain.model.SensorReading;

public interface ProcessReadingUseCase {
    /**
     * Procesa una lectura de sensor aplicando todas las reglas de negocio:
     * 1. Validación de consistencia (síncrona)
     * 2. Evaluación de emergencia con promedio de 3 sensores (síncrona)
     * 3. Consulta de zona sensible a ZoneService (síncrona)
     * 4. Publicación de alerta o evento de mantenimiento (asíncrona)
     */
    void process(SensorReading reading);
}
