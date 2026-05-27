package com.demo.alert.application.port.in;

import com.demo.alert.domain.model.AlertNotification;

public interface ProcessAlertUseCase {
    void process(AlertNotification notification);
}
