package org.example.svfrms.crop;

import org.example.svfrms.domain.SensorType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry of crop profiles (OCP: adding/removing a crop is a register/remove call or a
 * line in crops.csv, never an edit to the monitoring code).
 */
public final class CropRegistry implements CropCatalog {
    private final Map<String, CropProfile> crops = new ConcurrentHashMap<>();
    private final List<ThresholdRule> facilityRules;

    public CropRegistry(List<ThresholdRule> facilityRules) {
        this.facilityRules = List.copyOf(facilityRules);
    }

    public void register(CropProfile profile) {
        crops.put(profile.name(), profile);
    }

    public boolean remove(String cropName) {
        return crops.remove(cropName) != null;
    }

    public Optional<CropProfile> find(String cropName) {
        return Optional.ofNullable(crops.get(cropName));
    }

    public Set<String> cropNames() {
        return new TreeSet<>(crops.keySet());
    }

    public List<ThresholdRule> facilityRules() {
        return facilityRules;
    }

    @Override
    public List<ThresholdRule> rulesFor(String crop, SensorType sensor) {
        List<ThresholdRule> result = new ArrayList<>();
        find(crop).ifPresent(p -> result.addAll(p.rulesFor(sensor)));
        for (ThresholdRule rule : facilityRules) {
            if (rule.sensor() == sensor) {
                result.add(rule);
            }
        }
        return result;
    }
}
