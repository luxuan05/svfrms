package org.example.svfrms.sensor;

import org.example.svfrms.domain.SensorType;

/**
 * Where readings come from (DIP). The simulation uses random values; a real deployment or a
 * test can plug in hardware readings or a fixed script without touching the monitoring code.
 */
public interface SensorSource {
    double read(String chamberId, SensorType type);
}
