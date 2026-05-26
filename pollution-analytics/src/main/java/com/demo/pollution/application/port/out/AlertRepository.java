package com.demo.pollution.application.port.out;

import com.demo.pollution.domain.model.Alert;
import java.util.List;
import java.util.Optional;

public interface AlertRepository {
    Alert          save(Alert alert);
    List<Alert>    findAll();
    List<Alert>    findAllActive();
    Optional<Alert> findById(Long id);
}
