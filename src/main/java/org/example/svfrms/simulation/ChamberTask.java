package org.example.svfrms.simulation;

import org.example.svfrms.domain.SensorReading;
import org.example.svfrms.domain.SensorType;
import org.example.svfrms.monitoring.SensorProcessor;
import org.example.svfrms.resource.ResourceUsage;
import org.example.svfrms.sensor.SensorSource;

/**
 * Producer: simulates one chamber for a number of rounds (legacy ChamberThread, lines 275-295).
 * A Runnable instead of extending Thread, so it can be submitted to an ExecutorService.
 */
public final class ChamberTask implements Runnable {
    static final int BASELINE_WATER_PER_ROUND = 2;   // legacy line 291
    static final int BASELINE_ENERGY_PER_ROUND = 1;  // legacy line 292

    private final String chamberId;
    private final String crop;
    private final int rounds;
    private final SensorSource sensors;
    private final SensorProcessor processor;
    private final ResourceUsage usage;

    public ChamberTask(String chamberId, String crop, int rounds,
                       SensorSource sensors, SensorProcessor processor, ResourceUsage usage) {
        this.chamberId = chamberId;
        this.crop = crop;
        this.rounds = rounds;
        this.sensors = sensors;
        this.processor = processor;
        this.usage = usage;
    }

    @Override
    public void run() {
        try {
            for (int i = 0; i < rounds; i++) {
                for (SensorType type : SensorType.values()) {
                    double value = sensors.read(chamberId, type);
                    processor.process(new SensorReading(chamberId, crop, type, value));
                }
                usage.addWater(BASELINE_WATER_PER_ROUND);
                usage.addEnergy(BASELINE_ENERGY_PER_ROUND);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
