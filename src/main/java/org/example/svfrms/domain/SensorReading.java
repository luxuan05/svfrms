package org.example.svfrms.domain;

import java.util.Objects;

/** One measurement from one sensor in one chamber. Immutable, so safe to share between threads. */
public record SensorReading(String chamberId, String crop, SensorType type, double value) {
    public SensorReading {
        Objects.requireNonNull(chamberId, "chamberId");
        Objects.requireNonNull(crop, "crop");
        Objects.requireNonNull(type, "type");
    }
}
