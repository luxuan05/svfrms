package org.example.svfrms.crop;

import org.example.svfrms.domain.SensorType;

import java.util.List;
import java.util.Objects;

/** A crop and its threshold rules. Immutable. */
public record CropProfile(String name, List<ThresholdRule> rules) {

    public CropProfile {
        Objects.requireNonNull(name, "name");
        rules = List.copyOf(rules);
    }

    /** Rules for one sensor, in file order. */
    public List<ThresholdRule> rulesFor(SensorType sensor) {
        return rules.stream().filter(r -> r.sensor() == sensor).toList();
    }
}
