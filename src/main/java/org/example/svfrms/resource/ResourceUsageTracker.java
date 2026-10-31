package org.example.svfrms.resource;

/**
 * TODO (Person B - concurrency): thread-safe water/energy counters.
 *
 * Replaces legacy static ints totalWaterUsage / totalEnergyUsage (lines 21-22), which were
 * updated with "+=" from 4 chamber threads at once (race R2: lost updates).
 * Suggested fix: two java.util.concurrent.atomic.AtomicInteger fields (addAndGet / get).
 * Be ready to explain WHY "x += n" is not atomic (read, add, write = 3 steps).
 */
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread-safe water/energy counters.
 *
 * Replaces legacy static ints totalWaterUsage / totalEnergyUsage (lines 21-22), which were
 * updated with "+=" from 4 chamber threads at once (race R2: lost updates).
 * Fix: two java.util.concurrent.atomic.AtomicInteger fields (addAndGet / get).
 */
public final class ResourceUsageTracker implements ResourceUsage {

    // AtomicInteger provides lock-free, thread-safe updates using hardware-level CAS operations.
    private final AtomicInteger waterUsage = new AtomicInteger(0);
    private final AtomicInteger energyUsage = new AtomicInteger(0);

    @Override
    public void addWater(int litres) {
        // addAndGet is atomic: it safely reads, adds, and writes in one uninterruptible step.
        waterUsage.addAndGet(litres);
    }

    @Override
    public void addEnergy(int kwh) {
        energyUsage.addAndGet(kwh);
    }

    @Override
    public int totalWater() {
        // get() is thread-safe and returns the current value.
        return waterUsage.get();
    }

    @Override
    public int totalEnergy() {
        return energyUsage.get();
    }
}