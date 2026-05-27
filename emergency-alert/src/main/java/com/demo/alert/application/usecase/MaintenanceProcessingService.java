package com.demo.alert.application.usecase;

import com.demo.alert.application.port.in.ProcessMaintenanceUseCase;
import com.demo.alert.application.port.out.EmergencyNotifierPort;
import com.demo.alert.application.port.out.MaintenanceEventRepository;
import com.demo.alert.domain.model.MaintenanceEvent;
import org.springframework.stereotype.Service;

@Service
public class MaintenanceProcessingService implements ProcessMaintenanceUseCase {

    private final EmergencyNotifierPort    emergencyNotifierPort;
    private final MaintenanceEventRepository maintenanceEventRepository;

    public MaintenanceProcessingService(EmergencyNotifierPort emergencyNotifierPort,
                                        MaintenanceEventRepository maintenanceEventRepository) {
        this.emergencyNotifierPort     = emergencyNotifierPort;
        this.maintenanceEventRepository = maintenanceEventRepository;
    }

    @Override
    public void process(MaintenanceEvent event) {
        try {
            emergencyNotifierPort.notifyMaintenance(
                    event.getSensorId(), event.getZoneId(), event.jumpMagnitude());
            event.setStatus("NOTIFIED");
        } catch (Exception e) {
            event.setStatus("FAILED");
        } finally {
            maintenanceEventRepository.save(event);
        }
    }
}
