package com.demo.alert.application.port.in;

import com.demo.alert.domain.model.MaintenanceEvent;

public interface ProcessMaintenanceUseCase {
    void process(MaintenanceEvent event);
}
