package org.example.svfrms.monitoring;

import org.example.svfrms.alert.Alert;
import org.example.svfrms.alert.AlertListener;
import org.example.svfrms.alert.AlertSeverity;
import org.example.svfrms.control.ControlDispatcher;
import org.example.svfrms.control.ControlRequest;
import org.example.svfrms.crop.CropCatalog;
import org.example.svfrms.crop.ThresholdRule;
import org.example.svfrms.domain.ChamberRegistry;
import org.example.svfrms.domain.SensorReading;
import org.example.svfrms.logging.EventLogger;
import org.example.svfrms.resource.ResourceUsage;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Evaluates one sensor reading against the crop's rules. Replaces legacy processSensor
 * (lines 141-205): the 60-line crop/sensor if-chain becomes one loop over data.
 * Depends only on interfaces, all injected through the constructor (DIP).
 */
public final class SensorProcessor {
    private final CropCatalog crops;
    private final ChamberRegistry chambers;
    private final List<AlertListener> alertListeners;
    private final ResourceUsage usage;
    private final ControlDispatcher dispatcher;
    private final EventLogger logger;

    public SensorProcessor(CropCatalog crops, ChamberRegistry chambers, List<AlertListener> alertListeners,
                           ResourceUsage usage, ControlDispatcher dispatcher, EventLogger logger) {
        this.crops = crops;
        this.chambers = chambers;
        this.alertListeners = List.copyOf(alertListeners);
        this.usage = usage;
        this.dispatcher = dispatcher;
        this.logger = logger;
    }

    public void process(SensorReading reading) throws InterruptedException {
        logger.sensor("Processing " + reading.crop() + " " + reading.type().legacyName() + " = " + reading.value());
        chambers.state(reading.chamberId()).set(reading.type(), reading.value());

        for (ThresholdRule rule : crops.rulesFor(reading.crop(), reading.type())) {
            if (!rule.isBreachedBy(reading.value())) {
                continue;
            }
            // Same order of effects as legacy: track + record/print alert, add costs, request device.
            Alert alert = new Alert(reading.chamberId(), reading.crop(),
                    rule.alertMessage(reading.chamberId(), reading.crop()),
                    AlertSeverity.WARNING, Instant.now());
            for (AlertListener listener : alertListeners) {
                listener.onAlert(alert);
            }
            usage.addWater(rule.waterCost());
            usage.addEnergy(rule.energyCost());

            Optional<String> device = rule.action();
            if (device.isPresent()) {
                dispatcher.submit(new ControlRequest(reading.chamberId(), device.get()));
            }
        }
    }
}
