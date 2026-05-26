package com.demo.pollution.application.port.in;

import com.demo.pollution.domain.model.Alert;
import java.util.List;

public interface AlertQueryUseCase {
    List<Alert> findAllActive();
    List<Alert> findAll();
    Alert       findById(Long id);
}
