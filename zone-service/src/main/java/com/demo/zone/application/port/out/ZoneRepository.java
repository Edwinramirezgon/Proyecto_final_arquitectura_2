package com.demo.zone.application.port.out;

import com.demo.zone.domain.model.Zone;
import java.util.List;
import java.util.Optional;

public interface ZoneRepository {
    Zone           save(Zone zone);
    List<Zone>     findAll();
    Optional<Zone> findById(Long id);
    void           deleteById(Long id);
    boolean        existsById(Long id);
}
