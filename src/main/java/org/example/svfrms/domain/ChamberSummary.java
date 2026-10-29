package org.example.svfrms.domain;

/**
 * Per-chamber alert/action statistics for the report (legacy ChamberSummary, lines 41-46).
 * Written by the chamber thread (alerts) and the control thread (actions), so all access is synchronized.
 */
public final class ChamberSummary {
    private int alerts = 0;
    private int mitigationActions = 0;
    private String lastAction = "No action";
    private String lastAlert = "None";

    public synchronized void recordAlert(String message) {
        alerts++;
        lastAlert = message;
    }

    public synchronized void recordAction(String message) {
        mitigationActions++;
        lastAction = message;
    }

    /** Consistent copy of all four fields, taken under one lock. */
    public synchronized Snapshot snapshot() {
        return new Snapshot(alerts, mitigationActions, lastAction, lastAlert);
    }

    public record Snapshot(int alerts, int mitigationActions, String lastAction, String lastAlert) {}
}
