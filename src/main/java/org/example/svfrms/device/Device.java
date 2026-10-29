package org.example.svfrms.device;

import org.example.svfrms.domain.ChamberState;

/**
 * Strategy: one implementation per automated device, replacing the if/else chain in
 * legacy activateControl (lines 90-129). A new device (e.g. Heating Unit) is a new class
 * registered in DeviceRegistry - no existing code changes.
 */
public interface Device {

    /** Name used in crops.csv and log lines, e.g. "Cooling Fan". */
    String name();

    /**
     * Applies this device's effect to the chamber and returns the follow-up log line,
     * e.g. "Temperature reduced to 19.0C".
     */
    String apply(ChamberState state);
}
