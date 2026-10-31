package org.example.svfrms.device;

import org.example.svfrms.domain.ChamberState;
import org.example.svfrms.domain.SensorType;

/** Legacy lines 105-110: nutrient level +15, never above 100. */
public final class NutrientInjector implements Device {

    @Override
    public String name() {
        return "Nutrient Injector";
    }

    @Override
    public String apply(ChamberState state) {
        double n = state.adjust(SensorType.NUTRIENT, v -> Math.min(100, v + 15));
        return "Nutrient level increased to " + n + "%";
    }
}
