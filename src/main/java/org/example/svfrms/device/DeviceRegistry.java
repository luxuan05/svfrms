package org.example.svfrms.device;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Looks devices up by name.
 *
 * TODO (Person A): add WaterPump, NutrientInjector, Humidifier, Dehumidifier, GrowLight,
 * following CoolingFan. Port the exact effects and messages from legacy lines 99-128
 * (e.g. Water Pump: soil moisture +20, max 100, "Soil moisture increased to X%").
 */
public final class DeviceRegistry {
    private final Map<String, Device> devices = new ConcurrentHashMap<>();

    public DeviceRegistry register(Device device) {
        devices.put(device.name(), device);
        return this;
    }

    public Optional<Device> find(String name) {
        return Optional.ofNullable(devices.get(name));
    }
}
