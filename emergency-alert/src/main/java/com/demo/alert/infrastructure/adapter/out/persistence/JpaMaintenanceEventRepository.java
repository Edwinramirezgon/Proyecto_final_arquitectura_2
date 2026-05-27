package com.demo.alert.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaMaintenanceEventRepository extends JpaRepository<MaintenanceEventEntity, Long> {}
