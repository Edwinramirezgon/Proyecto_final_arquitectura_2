package com.demo.pollution.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface JpaAlertRepository extends JpaRepository<AlertEntity, Long> {
    List<AlertEntity> findByActiveTrue();
}
