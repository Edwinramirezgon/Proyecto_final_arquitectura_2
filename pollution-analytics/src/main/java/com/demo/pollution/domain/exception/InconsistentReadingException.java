package com.demo.pollution.domain.exception;

public class InconsistentReadingException extends RuntimeException {
    public InconsistentReadingException(String sensorId, double previous, double current) {
        super(String.format(
            "Lectura inconsistente del sensor '%s': salto de %.1f a %.1f µg/m³.",
            sensorId, previous, current));
    }
}
