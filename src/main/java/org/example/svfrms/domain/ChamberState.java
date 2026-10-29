package org.example.svfrms.domain;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.DoubleUnaryOperator;

/**
 * Current environmental values of one chamber.
 *
 * Concurrency (legacy race R4): the chamber thread writes readings while the control
 * thread does read-modify-write updates (e.g. temperature - 5). Legacy did both with no
 * locking, so updates could be lost. Every method here locks on this object, and
 * {@link #adjust} performs the read-modify-write as one atomic step.
 */
public final class ChamberState {
    private final Map<SensorType, Double> values = new EnumMap<>(SensorType.class);

    public ChamberState() {
        for (SensorType type : SensorType.values()) {
            values.put(type, type.initialValue());
        }
    }

    public synchronized void set(SensorType type, double value) {
        values.put(type, value);
    }

    public synchronized double get(SensorType type) {
        return values.get(type);
    }

    /** Atomically applies {@code change} to the current value and returns the new value. */
    public synchronized double adjust(SensorType type, DoubleUnaryOperator change) {
        double updated = change.applyAsDouble(values.get(type));
        values.put(type, updated);
        return updated;
    }
}
