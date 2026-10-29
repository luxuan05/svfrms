package org.example.svfrms;

import org.example.svfrms.config.ChamberAssignment;
import org.example.svfrms.config.ChamberConfigLoader;
import org.example.svfrms.config.ConfigReader;
import org.example.svfrms.config.CropProfileLoader;
import org.example.svfrms.crop.CropProfile;
import org.example.svfrms.crop.CropRegistry;
import org.example.svfrms.crop.ThresholdRule;

import java.io.BufferedReader;
import java.util.List;

/**
 * Composition root: the only place that creates concrete classes and connects them.
 * Everything else receives its dependencies through constructors (DIP).
 */
public final class App {
    static final int ROUNDS_PER_CHAMBER = 20;      // legacy line 285
    static final int CONTROL_QUEUE_CAPACITY = 5;   // legacy line 15

    public static void main(String[] args) throws Exception {
        CropRegistry crops;
        try (BufferedReader in = ConfigReader.open("crops.csv")) {
            crops = new CropProfileLoader().load(in);
        }
        List<ChamberAssignment> chambers;
        try (BufferedReader in = ConfigReader.open("chambers.csv")) {
            chambers = new ChamberConfigLoader().load(in);
        }

        // ---- Skeleton smoke test: proves config loading works. Remove once wiring is done. ----
        System.out.println("Crops loaded from " + ConfigReader.sourceOf("crops.csv"));
        for (String name : crops.cropNames()) {
            CropProfile profile = crops.find(name).orElseThrow();
            System.out.println("  " + name);
            profile.rules().forEach(r -> System.out.println("      " + r.describe()));
        }
        System.out.println("  Facility-wide (all crops)");
        for (ThresholdRule r : crops.facilityRules()) {
            System.out.println("      " + r.describe());
        }
        System.out.println("Chambers loaded from " + ConfigReader.sourceOf("chambers.csv"));
        for (ChamberAssignment c : chambers) {
            String warning = crops.find(c.crop()).isPresent() ? "" : "   (WARNING: no profile, facility rules only)";
            System.out.println("  " + c.chamberId() + " -> " + c.crop() + warning);
        }

        /* ---- TODO (Person B + C): wiring, roughly in this order ----
         *
         * ConsoleEventLogger logger  = new ConsoleEventLogger(System.out);
         * ChamberRegistry registry   = new ChamberRegistry();
         * ChamberTracker tracker     = new ChamberTracker(registry);
         * AlertRepository alerts     = new AlertRepository();
         * ResourceUsageTracker usage = new ResourceUsageTracker();
         * ControlQueue queue         = new ControlQueue(CONTROL_QUEUE_CAPACITY);
         * DeviceRegistry devices     = new DeviceRegistry().register(new CoolingFan()); // + others
         *
         * // Listener order = legacy order: TRACK_ALERT, then CREATE_ALERT (store, print)
         * SensorProcessor processor = new SensorProcessor(crops, registry,
         *         List.of(tracker, alerts, new ConsoleAlertNotifier(logger)), usage, queue, logger);
         *
         * 1. Start one ControlWorker on its own executor.
         * 2. Submit one ChamberTask per chamber to a fixed thread pool; shutdown() + awaitTermination().
         * 3. queue.submit(ControlRequest.shutdown()); wait for the worker to finish.
         * 4. Report + archive in ONE defined order (legacy race R5), then write archive/daily_report.txt.
         */
    }
}
