package org.example.svfrms.device;

import org.example.svfrms.domain.ChamberState;
import org.example.svfrms.domain.SensorType;

/** Legacy lines 99-104: soil moisture +20, never above 100. */
public final class WaterPump implements Device {

    @Override
    public String name() {
        return "Water Pump";
    }

    @Override
    public String apply(ChamberState state) {
        double m = state.adjust(SensorType.SOIL_MOISTURE, v -> Math.min(100, v + 20));
        return "Soil moisture increased to " + m + "%";
    }
}
