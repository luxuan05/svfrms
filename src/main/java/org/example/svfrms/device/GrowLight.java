package org.example.svfrms.device;

import org.example.svfrms.domain.ChamberState;
import org.example.svfrms.domain.SensorType;

/** Legacy lines 123-128: light level +150, never above 1000. */
public final class GrowLight implements Device {

    @Override
    public String name() {
        return "Grow Light";
    }

    @Override
    public String apply(ChamberState state) {
        double l = state.adjust(SensorType.LIGHT, v -> Math.min(1000, v + 150));
        return "Light level increased to " + l + " lux";
    }
}
