package com.demo.alert.application.port.out;

import com.demo.alert.domain.model.AlertNotification;
import java.util.List;

public interface AlertNotificationRepository {
    void                   save(AlertNotification notification);
    List<AlertNotification> findAll();
}
