package org.example.svfrms.report;

import java.util.List;

/**
 * A report rendered as lines of text. Printing to console or writing to a file is done by
 * the caller, so report content and output destination are separate responsibilities.
 *
 * TODO (Person C): DailyReport now; the interface is the extension point for the spec's
 * Weekly Crop / Water / Energy / Nutrient reports (spec 3.5).
 */
public interface Report {
    List<String> render();
}
