package org.example.svfrms.device;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Factory Method: callers ask for a device by the name used in crops.csv ("Cooling Fan") and get a
 * {@link Device} back, without ever naming or calling {@code new} on a concrete device class
 * (same idea as LockFactory.getLock(name) in Wk10).
 *
 * Adding a device (e.g. Heating Unit) = one new Device class + one {@link #register} call.
 * Legacy equivalent: the if/else chain on the device name in activateControl (lines 93-128).
 */
public final class DeviceFactory {
    // Read concurrently by all chamber threads, so a thread-safe map (Wk9).
    private final Map<String, Supplier<? extends Device>> makers = new ConcurrentHashMap<>();

    /** The six devices of the legacy system. */
    public static DeviceFactory withStandardDevices() {
        return new DeviceFactory()
                .register(CoolingFan::new)
                .register(WaterPump::new)
                .register(NutrientInjector::new)
                .register(Humidifier::new)
                .register(Dehumidifier::new)
                .register(GrowLight::new);
    }

    /** Registers a device type under its own {@link Device#name()}. */
    public DeviceFactory register(Supplier<? extends Device> maker) {
        makers.put(maker.get().name(), maker);
        return this;
    }

    /** Creates the device with this name, or empty if no such device exists (legacy ignored unknown names). */
    public Optional<Device> create(String name) {
        return Optional.ofNullable(makers.get(name)).map(Supplier::get);
    }

    public boolean supports(String name) {
        return makers.containsKey(name);
    }

    public Set<String> names() {
        return new TreeSet<>(makers.keySet());
    }
}
