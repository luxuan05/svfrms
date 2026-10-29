package org.example.svfrms.resource;

/**
 * TODO (Person B - concurrency): thread-safe water/energy counters.
 *
 * Replaces legacy static ints totalWaterUsage / totalEnergyUsage (lines 21-22), which were
 * updated with "+=" from 4 chamber threads at once (race R2: lost updates).
 * Suggested fix: two java.util.concurrent.atomic.AtomicInteger fields (addAndGet / get).
 * Be ready to explain WHY "x += n" is not atomic (read, add, write = 3 steps).
 */
public final class ResourceUsageTracker implements ResourceUsage {

    @Override
    public void addWater(int litres) {
        throw new UnsupportedOperationException("TODO Person B");
    }

    @Override
    public void addEnergy(int kwh) {
        throw new UnsupportedOperationException("TODO Person B");
    }

    @Override
    public int totalWater() {
        throw new UnsupportedOperationException("TODO Person B");
    }

    @Override
    public int totalEnergy() {
        throw new UnsupportedOperationException("TODO Person B");
    }
}
