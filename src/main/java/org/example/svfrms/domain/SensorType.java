package org.example.svfrms.domain;

/**
 * Sensor kinds supported by a chamber.
 * Declaration order = the order a chamber reads its sensors each round
 * (legacy ChamberThread.run, lines 286-290), so do not reorder.
 */
public enum SensorType {
    TEMPERATURE("Temperature", 24),
    HUMIDITY("Humidity", 70),
    SOIL_MOISTURE("Soil", 60),
    LIGHT("Light", 600),
    NUTRIENT("Nutrient", 70);

    private final String legacyName;
    private final double initialValue;

    SensorType(String legacyName, double initialValue) {
        this.legacyName = legacyName;
        this.initialValue = initialValue;
    }

    /** Name used in log messages, e.g. "Processing Tomato Temperature = 31.0". */
    public String legacyName() { return legacyName; }

    /** Starting value for a new chamber (legacy ChamberState defaults, lines 34-38). */
    public double initialValue() { return initialValue; }
}
