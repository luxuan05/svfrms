package org.example.svfrms.crop;

import org.example.svfrms.domain.SensorType;

import java.util.List;

/**
 * What the monitoring code needs to know about crops (DIP: SensorProcessor depends on this
 * abstraction, not on how crops are stored or loaded).
 */
public interface CropCatalog {

    /** All rules to evaluate for this crop and sensor: crop-specific rules first, then facility-wide rules. */
    List<ThresholdRule> rulesFor(String crop, SensorType sensor);
}
