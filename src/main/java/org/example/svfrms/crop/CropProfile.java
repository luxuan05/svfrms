package org.example.svfrms.crop;

import org.example.svfrms.domain.SensorType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** A crop and its threshold rules. Immutable, so it can be shared between threads without locking. */
public record CropProfile(String name, List<ThresholdRule> rules) {

    public CropProfile {
        Objects.requireNonNull(name, "name");
        rules = List.copyOf(rules);
    }

    /**
     * Builder pattern, e.g.
     * <pre>
     * CropProfile.builder("Kale")
     *     .rule(ThresholdRule.builder(TEMPERATURE).above(24).label("High Temp").device("Cooling Fan"))
     *     .rule(ThresholdRule.builder(TEMPERATURE).below(7).label("Low Temp"))
     *     .build();
     * </pre>
     */
    public static Builder builder(String name) {
        return new Builder(name);
    }

    /** Rules for one sensor, in the order they were added. */
    public List<ThresholdRule> rulesFor(SensorType sensor) {
        return rules.stream().filter(r -> r.sensor() == sensor).toList();
    }

    public static final class Builder {
        private final String name;
        private final List<ThresholdRule> rules = new ArrayList<>();

        private Builder(String name) {
            this.name = name;
        }

        public Builder rule(ThresholdRule rule) {
            rules.add(rule);
            return this;
        }

        public Builder rule(ThresholdRule.Builder rule) {
            return rule(rule.build());
        }

        public CropProfile build() {
            return new CropProfile(name, rules);
        }
    }
}
