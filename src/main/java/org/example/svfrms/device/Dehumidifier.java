package org.example.svfrms.device;

import org.example.svfrms.domain.ChamberState;
import org.example.svfrms.domain.SensorType;

/** Legacy lines 117-122: humidity -10, never below 30. */
public final class Dehumidifier implements Device {

    @Override
    public String name() {
        return "Dehumidifier";
    }

    @Override
    public String apply(ChamberState state) {
        double h = state.adjust(SensorType.HUMIDITY, v -> Math.max(30, v - 10));
        return "Humidity reduced to " + h + "%";
    }
}
