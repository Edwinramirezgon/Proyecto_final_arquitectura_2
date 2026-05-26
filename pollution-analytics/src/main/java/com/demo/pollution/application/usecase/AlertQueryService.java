package com.demo.pollution.application.usecase;

import com.demo.pollution.application.port.in.AlertQueryUseCase;
import com.demo.pollution.application.port.out.AlertRepository;
import com.demo.pollution.domain.model.Alert;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AlertQueryService implements AlertQueryUseCase {

    private final AlertRepository alertRepository;

    public AlertQueryService(AlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    @Override
    public List<Alert> findAllActive() {
        return alertRepository.findAllActive();
    }

    @Override
    public List<Alert> findAll() {
        return alertRepository.findAll();
    }

    @Override
    public Alert findById(Long id) {
        return alertRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Alerta no encontrada con id: " + id));
    }
}
