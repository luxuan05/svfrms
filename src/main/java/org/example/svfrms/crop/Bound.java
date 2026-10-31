package org.example.svfrms.crop;

/** Which side of the limit counts as a breach. Legacy uses strict comparisons, so the limit itself is safe. */
public enum Bound {
    /** Breached when value > limit. */
    ABOVE {
        @Override
        boolean isBreached(double value, double limit) {
            return value > limit;
        }
    },
    /** Breached when value < limit. */
    BELOW {
        @Override
        boolean isBreached(double value, double limit) {
            return value < limit;
        }
    };

    abstract boolean isBreached(double value, double limit);
}
