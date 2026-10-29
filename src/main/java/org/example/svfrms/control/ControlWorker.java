package org.example.svfrms.control;

import org.example.svfrms.device.Device;
import org.example.svfrms.device.DeviceRegistry;
import org.example.svfrms.domain.ChamberRegistry;
import org.example.svfrms.logging.EventLogger;
import org.example.svfrms.tracking.ChamberTracker;

import java.util.Optional;

/**
 * Consumer: takes control requests and runs the matching device.
 * Replaces legacy ControlThread (lines 251-273) + the bookkeeping in activateControl.
 * A Runnable instead of a Thread subclass, so an ExecutorService can run it.
 */
public final class ControlWorker implements Runnable {
    private final ControlQueue queue;
    private final DeviceRegistry devices;
    private final ChamberRegistry chambers;
    private final ChamberTracker tracker;
    private final EventLogger logger;

    public ControlWorker(ControlQueue queue, DeviceRegistry devices, ChamberRegistry chambers,
                         ChamberTracker tracker, EventLogger logger) {
        this.queue = queue;
        this.devices = devices;
        this.chambers = chambers;
        this.tracker = tracker;
        this.logger = logger;
    }

    @Override
    public void run() {
        try {
            while (true) {
                ControlRequest request = queue.take();
                if (request.isShutdown()) {
                    return;   // FIFO: everything queued before the pill has been processed
                }
                execute(request);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();   // keep the interrupt visible to the executor
        }
    }

    private void execute(ControlRequest request) {
        Optional<Device> device = devices.find(request.deviceName());
        if (device.isEmpty()) {
            return;   // legacy activateControl silently ignored unknown devices
        }
        String chamberId = request.chamberId();
        String activated = device.get().name() + " Activated";
        String result = device.get().apply(chambers.state(chamberId));
        tracker.recordAction(chamberId, activated);
        logger.control(chamberId, activated);
        logger.control(chamberId, result);
    }
}
