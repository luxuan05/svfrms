package org.example.svfrms.alert;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO (Person B - concurrency): stores active + archived alerts and the "generated" counter.
 *
 * Replaces legacy: alerts / archivedAlerts lists, totalAlerts, totalAlertsGenerated
 * (lines 10-11, 23-24, 73-75) and the list work in archiveAlerts (lines 220-241).
 *
 * Requirements:
 *  - ONE lock guards both lists and the counter, so "list and counter agree" always holds
 *    (fixes race R1). Writers and readers must use the same lock - in legacy, CREATE_ALERT
 *    never took alertLock, so generateDailyReport's locking protected nothing.
 *  - No method may call out to other locked objects while holding this lock
 *    (no nested locks = no lock-ordering deadlock, fixes D1).
 *  - Return copies / snapshots, never the internal lists.
 *  - "Active alerts" count = number of active (unarchived) alerts, as legacy totalAlerts.
 */

public final class AlertRepository implements AlertListener {

    // The ONE lock that guards all internal state.
    private final Object lock = new Object();
    
    private final List<Alert> activeAlerts = new ArrayList<>();
    private final List<Alert> archivedAlerts = new ArrayList<>();
    private int totalAlertsGenerated = 0;

    @Override
    public void onAlert(Alert alert) {
        synchronized (lock) {
            activeAlerts.add(alert);
            totalAlertsGenerated++;
        }
    }

    /** 
     * Moves all active alerts to the archive atomically; returns how many were moved. 
     */
    public int archiveAll() {
        synchronized (lock) {
            int movedCount = activeAlerts.size();
            if (movedCount > 0) {
                archivedAlerts.addAll(activeAlerts);
                activeAlerts.clear();
            }
            return movedCount;
        }
    }

    public int generatedCount() {
        synchronized (lock) {
            return totalAlertsGenerated;
        }
    }

    public int activeCount() {
        synchronized (lock) {
            return activeAlerts.size();
        }
    }

    /** Returns a snapshot (copy) of the archived alerts. */
    public List<Alert> archivedAlerts() {
        synchronized (lock) {
            return new ArrayList<>(archivedAlerts);
        }
    }
}