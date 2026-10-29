package org.example.svfrms.alert;

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

    @Override
    public void onAlert(Alert alert) {
        throw new UnsupportedOperationException("TODO Person B: add to active list and increment generated count");
    }

    /** Moves all active alerts to the archive atomically; returns how many were moved. */
    public int archiveAll() {
        throw new UnsupportedOperationException("TODO Person B");
    }

    public int generatedCount() {
        throw new UnsupportedOperationException("TODO Person B");
    }

    public int activeCount() {
        throw new UnsupportedOperationException("TODO Person B");
    }

    public List<Alert> archivedAlerts() {
        throw new UnsupportedOperationException("TODO Person B: return a copy");
    }
}
