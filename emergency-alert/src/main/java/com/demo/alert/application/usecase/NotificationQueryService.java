package com.demo.alert.application.usecase;

import com.demo.alert.application.port.in.NotificationQueryUseCase;
import com.demo.alert.application.port.out.AlertNotificationRepository;
import com.demo.alert.domain.model.AlertNotification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationQueryService implements NotificationQueryUseCase {

    private final AlertNotificationRepository repository;

    public NotificationQueryService(AlertNotificationRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<AlertNotification> findAll() {
        return repository.findAll();
    }
}
