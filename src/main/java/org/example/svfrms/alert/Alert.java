package org.example.svfrms.alert;

import java.time.Instant;

/**
 * A raised alert. Immutable, replacing the legacy plain String in the alerts list.
 * Reports should print {@link #message()} to keep the legacy output format.
 */
public record Alert(String chamberId, String crop, String message, AlertSeverity severity, Instant raisedAt) {}
