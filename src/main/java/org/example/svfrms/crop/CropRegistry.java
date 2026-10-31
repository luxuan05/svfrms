package org.example.svfrms.crop;

import org.example.svfrms.domain.SensorType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Registry of crop profiles (OCP: adding/removing a crop is a register/remove call or a
 * line in crops.csv, never an edit to the monitoring code).
 *
 * Concurrency: every sensor reading on every chamber thread looks up rules here, but crops are
 * only added or removed occasionally. A ReentrantReadWriteLock lets all readers in at once and
 * only makes them wait while a crop is being added or removed (Wk10).
 */
public final class CropRegistry implements CropCatalog {
    private final Map<String, CropProfile> crops = new HashMap<>();
    private final List<ThresholdRule> facilityRules;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock readLock = lock.readLock();
    private final Lock writeLock = lock.writeLock();

    public CropRegistry(List<ThresholdRule> facilityRules) {
        this.facilityRules = List.copyOf(facilityRules);
    }

    public void register(CropProfile profile) {
        writeLock.lock();
        try {
            crops.put(profile.name(), profile);
        } finally {
            writeLock.unlock();
        }
    }

    public boolean remove(String cropName) {
        writeLock.lock();
        try {
            return crops.remove(cropName) != null;
        } finally {
            writeLock.unlock();
        }
    }

    public Optional<CropProfile> find(String cropName) {
        readLock.lock();
        try {
            return Optional.ofNullable(crops.get(cropName));
        } finally {
            readLock.unlock();
        }
    }

    public Set<String> cropNames() {
        readLock.lock();
        try {
            return new TreeSet<>(crops.keySet());
        } finally {
            readLock.unlock();
        }
    }

    public List<ThresholdRule> facilityRules() {
        return facilityRules;
    }

    @Override
    public List<ThresholdRule> rulesFor(String crop, SensorType sensor) {
        List<ThresholdRule> result = new ArrayList<>();
        // The profile is immutable, so only the lookup needs the lock.
        find(crop).ifPresent(p -> result.addAll(p.rulesFor(sensor)));
        for (ThresholdRule rule : facilityRules) {
            if (rule.sensor() == sensor) {
                result.add(rule);
            }
        }
        return result;
    }
}
