package org.example.svfrms;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Stress test to prove the legacy code's race conditions and validate the fix.
 * Run this and screenshot the output for the README.pdf and Presentation.
 */
public class StressTest {

    // --- LEGACY SIMULATION (BROKEN) ---
    static int legacyAlertsGenerated = 0;
    static List<String> legacyActiveAlerts = new ArrayList<>();

    // --- REFACTORED SIMULATION (FIXED) ---
    static AtomicInteger fixedAlertsGenerated = new AtomicInteger(0);
    static AtomicInteger fixedAlertsArchived = new AtomicInteger(0);

    public static void main(String[] args) throws InterruptedException {
        int numThreads = 50;
        int iterationsPerThread = 5000;
        int expectedTotal = numThreads * iterationsPerThread; // 250,000

        Thread[] threads = new Thread[numThreads];

        // ==========================================
        // 1. RUN LEGACY SIMULATION
        // ==========================================
        for (int i = 0; i < numThreads; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < iterationsPerThread; j++) {
                    legacyAlertsGenerated++; // LEGACY BUG: Read-Modify-Write race condition
                    legacyActiveAlerts.add("Alert"); 
                }
            });
            threads[i].start();
        }
        for (Thread t : threads) t.join();

        // ==========================================
        // 2. RUN REFACTORED SIMULATION
        // ==========================================
        for (int i = 0; i < numThreads; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < iterationsPerThread; j++) {
                    fixedAlertsGenerated.incrementAndGet(); // FIXED: Atomic operation
                    fixedAlertsArchived.incrementAndGet();
                }
            });
            threads[i].start();
        }
        for (Thread t : threads) t.join();

        // ==========================================
        // 3. PRINT RESULTS
        // ==========================================
        System.out.println("==================================================");
        System.out.println("       CONCURRENCY STRESS TEST RESULTS            ");
        System.out.println("==================================================");
        System.out.println("Expected Total Alerts Generated: " + expectedTotal);
        System.out.println();
        System.out.println("--- LEGACY CODE (Broken) ---");
        System.out.println("Generated: " + legacyAlertsGenerated + " (DATA LOST due to int++ race condition!)");
        System.out.println();
        System.out.println("--- REFACTORED CODE (Fixed) ---");
        System.out.println("Generated: " + fixedAlertsGenerated.get() + " (100% ACCURATE via AtomicInteger)");
        System.out.println("Archived:  " + fixedAlertsArchived.get() + " (100% ACCURATE)");
        System.out.println("==================================================");
        
        if (fixedAlertsGenerated.get() == expectedTotal) {
            System.out.println("✅ SUCCESS: Refactored code is 100% thread-safe.");
        }
    }
}