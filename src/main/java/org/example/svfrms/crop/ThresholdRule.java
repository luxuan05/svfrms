package org.example.svfrms.crop;

import org.example.svfrms.domain.SensorType;

import java.util.Objects;
import java.util.Optional;

/**
 * One threshold check, replacing one hard-coded "if" in legacy processSensor (lines 141-205).
 *
 * @param sensor       which sensor this rule watches
 * @param bound        ABOVE or BELOW
 * @param limit        the threshold value
 * @param label        alert text suffix, e.g. "High Temp"
 * @param device       device name to activate when breached, or null for alert-only
 * @param waterCost    litres added to water usage when breached
 * @param energyCost   kWh added to energy usage when breached
 * @param facilityWide true if the rule applies to every crop (legacy Light/Nutrient checks)
 */
public record ThresholdRule(SensorType sensor, Bound bound, double limit, String label,
                            String device, int waterCost, int energyCost, boolean facilityWide) {

    public ThresholdRule {
        Objects.requireNonNull(sensor, "sensor");
        Objects.requireNonNull(bound, "bound");
        Objects.requireNonNull(label, "label");
        if (waterCost < 0 || energyCost < 0) {
            throw new IllegalArgumentException("costs must not be negative");
        }
    }

    public boolean isBreachedBy(double value) {
        return bound == Bound.ABOVE ? value > limit : value < limit;
    }

    /** Device to request when breached; empty for alert-only rules. */
    public Optional<String> action() {
        return Optional.ofNullable(device);
    }

    /**
     * Alert text in the exact legacy format:
     * crop rule -> "CH1 Tomato High Temp", facility-wide rule -> "CH1 Low Light Level".
     */
    public String alertMessage(String chamberId, String crop) {
        return facilityWide
                ? chamberId + " " + label
                : chamberId + " " + crop + " " + label;
    }

    /** Human-readable form, e.g. "Temperature > 28.0 -> High Temp [Cooling Fan]". */
    public String describe() {
        return sensor.legacyName() + (bound == Bound.ABOVE ? " > " : " < ") + limit
                + " -> " + label + action().map(d -> " [" + d + "]").orElse("");
    }
}
