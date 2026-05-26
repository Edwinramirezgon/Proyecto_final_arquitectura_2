package com.demo.zone.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaZoneRepository extends JpaRepository<ZoneEntity, Long> {}
