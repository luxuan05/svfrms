package org.example.svfrms.crop;

import org.example.svfrms.domain.SensorType;

import java.util.Objects;
import java.util.Optional;

/**
 * One threshold check, replacing one hard-coded "if" in legacy processSensor (lines 145-204).
 * Create one with {@link #builder(SensorType)} (Builder pattern) instead of the 8-argument constructor.
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

    public static Builder builder(SensorType sensor) {
        return new Builder(sensor);
    }

    public boolean isBreachedBy(double value) {
        return bound.isBreached(value, limit);
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

    /**
     * Builder: names every value, so a rule reads like the spec ("Temperature above 28 -> High Temp,
     * Cooling Fan") instead of eight positional arguments that are easy to swap by mistake.
     */
    public static final class Builder {
        private final SensorType sensor;
        private Bound bound;
        private double limit;
        private String label;
        private String device;
        private int waterCost;
        private int energyCost;
        private boolean facilityWide;

        private Builder(SensorType sensor) {
            this.sensor = sensor;
        }

        public Builder above(double limit) {
            return bound(Bound.ABOVE, limit);
        }

        public Builder below(double limit) {
            return bound(Bound.BELOW, limit);
        }

        public Builder bound(Bound bound, double limit) {
            this.bound = bound;
            this.limit = limit;
            return this;
        }

        public Builder label(String label) {
            this.label = label;
            return this;
        }

        public Builder device(String device) {
            this.device = device;
            return this;
        }

        public Builder waterCost(int litres) {
            this.waterCost = litres;
            return this;
        }

        public Builder energyCost(int kwh) {
            this.energyCost = kwh;
            return this;
        }

        public Builder facilityWide() {
            this.facilityWide = true;
            return this;
        }

        public ThresholdRule build() {
            return new ThresholdRule(sensor, bound, limit, label, device, waterCost, energyCost, facilityWide);
        }
    }
}
