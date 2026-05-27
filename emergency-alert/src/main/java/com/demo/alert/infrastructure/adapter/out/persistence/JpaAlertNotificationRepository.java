package com.demo.alert.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaAlertNotificationRepository extends JpaRepository<AlertNotificationEntity, Long> {}
