package org.example.svfrms.control;

/**
 * Command: "activate this device in this chamber". Replaces the legacy "CH1|Cooling Fan"
 * strings that were built and split again (lines 136, 269).
 */
public record ControlRequest(String chamberId, String deviceName) {

    private static final ControlRequest SHUTDOWN = new ControlRequest("<shutdown>", "<shutdown>");

    /** Poison pill: tells the worker to stop after everything queued before it is done. */
    public static ControlRequest shutdown() {
        return SHUTDOWN;
    }

    public boolean isShutdown() {
        return this == SHUTDOWN;   // identity on purpose: only the real sentinel stops the worker
    }
}
