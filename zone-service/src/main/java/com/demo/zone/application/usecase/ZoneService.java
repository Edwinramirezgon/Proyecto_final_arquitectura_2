package com.demo.zone.application.usecase;

import com.demo.zone.application.port.in.ZoneUseCase;
import com.demo.zone.application.port.out.ZoneRepository;
import com.demo.zone.domain.exception.ZoneNotFoundException;
import com.demo.zone.domain.model.Zone;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class ZoneService implements ZoneUseCase {

    private final ZoneRepository zoneRepository;

    public ZoneService(ZoneRepository zoneRepository) {
        this.zoneRepository = zoneRepository;
    }

    @Override
    public Zone create(Zone zone) {
        return zoneRepository.save(zone);
    }

    @Override
    public List<Zone> findAll() {
        return zoneRepository.findAll();
    }

    @Override
    public Zone findById(Long id) {
        return zoneRepository.findById(id)
                .orElseThrow(() -> new ZoneNotFoundException(id));
    }

    @Override
    public Zone update(Long id, Zone updated) {
        Zone existing = findById(id);
        existing.setName(updated.getName());
        existing.setLatitude(updated.getLatitude());
        existing.setLongitude(updated.getLongitude());
        existing.setRadiusKm(updated.getRadiusKm());
        existing.setSensitiveType(updated.getSensitiveType());
        existing.setPriority(updated.getSensitiveType().getBasePriority());
        return zoneRepository.save(existing);
    }

    @Override
    public void deleteById(Long id) {
        if (!zoneRepository.existsById(id))
            throw new ZoneNotFoundException(id);
        zoneRepository.deleteById(id);
    }

    @Override
    public Optional<Zone> findNearest(double latitude, double longitude, double radiusKm) {
        return zoneRepository.findAll().stream()
                .filter(z -> z.isNear(latitude, longitude))
                .max(Comparator.comparingInt(Zone::getPriority));
    }
}
