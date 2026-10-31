package org.example.svfrms;

import org.example.svfrms.alert.AlertRepository;
import org.example.svfrms.config.ChamberAssignment;
import org.example.svfrms.config.ChamberConfigLoader;
import org.example.svfrms.config.ConfigReader;
import org.example.svfrms.config.CropProfileLoader;
import org.example.svfrms.control.ControlQueue;
import org.example.svfrms.control.ControlRequest;
import org.example.svfrms.control.ControlWorker;
import org.example.svfrms.crop.CropProfile;
import org.example.svfrms.crop.CropRegistry;
import org.example.svfrms.crop.ThresholdRule;
import org.example.svfrms.device.CoolingFan;
import org.example.svfrms.device.DeviceRegistry;
import org.example.svfrms.resource.ResourceUsageTracker;
import org.example.svfrms.tracking.ChamberRegistry;
import org.example.svfrms.tracking.ChamberTask;
import org.example.svfrms.tracking.ChamberTracker;
import org.example.svfrms.tracking.ConsoleEventLogger;
import org.example.svfrms.tracking.SensorProcessor;
// TODO (Person C): import org.example.svfrms.report.DailyReport;

import java.io.BufferedReader;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Composition root: the only place that creates concrete classes and connects them.
 * Everything else receives its dependencies through constructors (DIP).
 */
public final class App {
    static final int ROUNDS_PER_CHAMBER = 20;      // legacy line 285
    static final int CONTROL_QUEUE_CAPACITY = 5;   // legacy line 15

    public static void main(String[] args) throws Exception {
        // 1. Load Configuration
        CropRegistry crops;
        try (BufferedReader in = ConfigReader.open("crops.csv")) {
            crops = new CropProfileLoader().load(in);
        }
        List<ChamberAssignment> chambers;
        try (BufferedReader in = ConfigReader.open("chambers.csv")) {
            chambers = new ChamberConfigLoader().load(in);
        }

        // ---- Skeleton smoke test: proves config loading works. ----
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

        System.out.println("\n--- Starting Simulation ---");

        // 2. Initialize Shared Concurrency Components (Person B)
        ConsoleEventLogger logger = new ConsoleEventLogger(System.out);
        ChamberRegistry registry = new ChamberRegistry();
        ChamberTracker tracker = new ChamberTracker(registry);
        AlertRepository alerts = new AlertRepository();
        ResourceUsageTracker usage = new ResourceUsageTracker();
        ControlQueue queue = new ControlQueue(CONTROL_QUEUE_CAPACITY);
        
        // Person A will add more devices here (WaterPump, NutrientInjector, etc.)
        DeviceRegistry devices = new DeviceRegistry().register(new CoolingFan()); 

        // Listener order = legacy order: TRACK_ALERT, then CREATE_ALERT (store, print)
        // Note: Adjust constructor args based on Person A/C's actual SensorProcessor signature
        SensorProcessor processor = new SensorProcessor(crops, registry,
                List.of(tracker, alerts, new ConsoleAlertNotifier(logger)), usage, queue, logger);

        // 3. Start Control Worker (Consumer) on its own thread
        ControlWorker worker = new ControlWorker(queue, devices, logger);
        Thread workerThread = new Thread(worker, "ControlWorker");
        workerThread.start();

        // 4. Submit Chamber Tasks (Producers) to a fixed thread pool
        ExecutorService chamberExecutor = Executors.newFixedThreadPool(chambers.size());
        
        for (ChamberAssignment assignment : chambers) {
            ChamberTask task = new ChamberTask(
                    assignment.chamberId(),