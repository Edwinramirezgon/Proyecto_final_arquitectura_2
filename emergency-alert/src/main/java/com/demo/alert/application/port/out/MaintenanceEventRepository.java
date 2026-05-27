package com.demo.alert.application.port.out;

import com.demo.alert.domain.model.MaintenanceEvent;

public interface MaintenanceEventRepository {
    void save(MaintenanceEvent event);
}
