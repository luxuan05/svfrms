package org.example.svfrms.alert;

import org.example.svfrms.logging.EventLogger;

/** Prints each alert to the console (the println part of legacy CREATE_ALERT, line 76). */
public final class ConsoleAlertNotifier implements AlertListener {
    private final EventLogger logger;

    public ConsoleAlertNotifier(EventLogger logger) {
        this.logger = logger;
    }

    @Override
    public void onAlert(Alert alert) {
        logger.alert(alert.chamberId(), alert.crop(), alert.message());
    }
}
