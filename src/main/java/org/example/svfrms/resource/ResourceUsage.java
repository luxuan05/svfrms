package org.example.svfrms.resource;

/** Facility-wide water and energy accounting. */
public interface ResourceUsage {
    void addWater(int litres);
    void addEnergy(int kwh);
    int totalWater();
    int totalEnergy();
}
