package org.example.svfrms.report;

import org.example.svfrms.alert.AlertRepository;
import org.example.svfrms.resource.ResourceUsage;

import java.util.List;

/**
 * TODO (Person C): the console daily report (legacy generateDailyReport, lines 207-218).
 * Lines, in order:
 *   ===== DAILY REPORT =====
 *   Generated Alerts: N
 *   Active Alerts: N
 *   Water Usage: N L
 *   Energy Usage: N kWh
 * The file report (legacy saveArchiveReport, lines 309-347) repeats these lines plus more:
 * build it by REUSING this class instead of duplicating the format (legacy duplicated it).
 */
public final class DailyReport implements Report {
    private final AlertRepository alerts;
    private final ResourceUsage usage;

    public DailyReport(AlertRepository alerts, ResourceUsage usage) {
        this.alerts = alerts;
        this.usage = usage;
    }

    @Override
    public List<String> render() {
        throw new UnsupportedOperationException("TODO Person C");
    }
}
