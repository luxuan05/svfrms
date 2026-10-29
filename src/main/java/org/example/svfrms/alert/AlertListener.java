package org.example.svfrms.alert;

/**
 * Observer: anything that reacts to a new alert (storage, console, per-chamber stats).
 * Replaces the legacy CREATE_ALERT / TRACK_ALERT branches of handleEvent (lines 72-82).
 */
@FunctionalInterface
public interface AlertListener {
    void onAlert(Alert alert);
}
