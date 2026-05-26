package com.demo.zone.domain.exception;

public class ZoneNotFoundException extends RuntimeException {
    public ZoneNotFoundException(Long id) {
        super(String.format("La zona con id '%d' no fue encontrada.", id));
    }
}
