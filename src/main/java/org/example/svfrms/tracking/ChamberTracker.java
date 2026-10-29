package org.example.svfrms.tracking;

import org.example.svfrms.alert.Alert;
import org.example.svfrms.alert.AlertListener;
import org.example.svfrms.domain.ChamberRegistry;

/** Updates per-chamber statistics (legacy TRACK_ALERT / TRACK_ACTION, lines 78-87). */
public final class ChamberTracker implements AlertListener {
    private final ChamberRegistry chambers;

    public ChamberTracker(ChamberRegistry chambers) {
        this.chambers = chambers;
    }

    @Override
    public void onAlert(Alert alert) {
        chambers.summary(alert.chamberId()).recordAlert(alert.message());
    }

    public void recordAction(String chamberId, String action) {
        chambers.summary(chamberId).recordAction(action);
    }
}
