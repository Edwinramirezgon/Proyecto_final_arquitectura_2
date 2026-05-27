package com.demo.alert.domain.exception;

public class NotificationDeliveryException extends RuntimeException {
    public NotificationDeliveryException(String destination, String cause) {
        super(String.format("No se pudo enviar la notificación a '%s': %s", destination, cause));
    }
}
