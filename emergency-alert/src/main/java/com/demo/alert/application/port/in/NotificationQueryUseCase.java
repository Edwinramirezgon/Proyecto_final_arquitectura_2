package com.demo.alert.application.port.in;

import com.demo.alert.domain.model.AlertNotification;
import java.util.List;

public interface NotificationQueryUseCase {
    List<AlertNotification> findAll();
}
