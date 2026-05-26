package com.demo.pollution.application.port.out;

import com.demo.pollution.domain.model.Alert;

public interface AlertPublisherPort {
    void publish(Alert alert);
}
