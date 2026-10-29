package org.example.svfrms.domain;

import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Owns every chamber's state and summary.
 *
 * Concurrency (legacy race R3): legacy getChamberState/getChamberSummary (lines 48-60) did
 * check-then-act on a plain HashMap from several threads. computeIfAbsent on a
 * ConcurrentHashMap creates each entry exactly once, atomically.
 */
public final class ChamberRegistry {
    private final ConcurrentMap<String, ChamberState> states = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, ChamberSummary> summaries = new ConcurrentHashMap<>();

    public ChamberState state(String chamberId) {
        return states.computeIfAbsent(chamberId, id -> new ChamberState());
    }

    public ChamberSummary summary(String chamberId) {
        return summaries.computeIfAbsent(chamberId, id -> new ChamberSummary());
    }

    /** Snapshots of all chambers that have a summary, sorted by chamber id (legacy used a TreeSet). */
    public SortedMap<String, ChamberSummary.Snapshot> summarySnapshots() {
        SortedMap<String, ChamberSummary.Snapshot> result = new TreeMap<>();
        summaries.forEach((id, summary) -> result.put(id, summary.snapshot()));
        return result;
    }
}
