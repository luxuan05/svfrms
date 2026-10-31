package org.example.svfrms.device;

import org.example.svfrms.domain.ChamberState;
import org.example.svfrms.domain.SensorType;

/** Legacy lines 111-116: humidity +10, never above 100. */
public final class Humidifier implements Device {

    @Override
    public String name() {
        return "Humidifier";
    }

    @Override
    public String apply(ChamberState state) {
        double h = state.adjust(SensorType.HUMIDITY, v -> Math.min(100, v + 10));
        return "Humidity increased to " + h + "%";
    }
}
