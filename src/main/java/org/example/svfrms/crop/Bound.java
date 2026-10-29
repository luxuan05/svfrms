package org.example.svfrms.crop;

/** Which side of the limit counts as a breach. */
public enum Bound {
    /** Breached when value > limit. */
    ABOVE,
    /** Breached when value < limit. */
    BELOW
}
