package com.demo.pollution.domain.model;

public enum ResolutionReason {
    NORMALIZED, // el promedio de la zona bajó del umbral de resolución
    TIMEOUT     // la alerta superó el tiempo máximo sin normalizarse
}
