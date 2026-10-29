package org.example.svfrms.sensor;

import org.example.svfrms.domain.SensorType;

import java.util.Random;

/**
 * Simulated sensors with the same value ranges as legacy ChamberThread.run (lines 286-290).
 * The seeded constructor makes runs repeatable, which is useful for comparing old vs new output.
 */
public final class RandomSensorSource implements SensorSource {
    private final Random random;   // java.util.Random is thread-safe

    public RandomSensorSource() {
        this.random = new Random();
    }

    public RandomSensorSource(long seed) {
        this.random = new Random(seed);
    }

    @Override
    public double read(String chamberId, SensorType type) {
        return switch (type) {
            case TEMPERATURE -> 10 + random.nextInt(30);
            case HUMIDITY -> 40 + random.nextInt(60);
            case SOIL_MOISTURE -> 20 + random.nextInt(60);
            case LIGHT -> 300 + random.nextInt(900);
            case NUTRIENT -> 20 + random.nextInt(80);
        };
    }
}
