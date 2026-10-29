package org.example.svfrms.device;

import org.example.svfrms.domain.ChamberState;
import org.example.svfrms.domain.SensorType;

/** Legacy lines 93-98: temperature -5, never below 18. */
public final class CoolingFan implements Device {

    @Override
    public String name() {
        return "Cooling Fan";
    }

    @Override
    public String apply(ChamberState state) {
        double t = state.adjust(SensorType.TEMPERATURE, v -> Math.max(18, v - 5));
        return "Temperature reduced to " + t + "C";
    }
}
