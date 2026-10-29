package org.example.svfrms.logging;

import java.io.PrintStream;
import java.util.Date;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/** Console implementation that reproduces the legacy output format exactly. */
public final class ConsoleEventLogger implements EventLogger {
    private static final String GREEN = "\u001B[32m";
    private static final String RESET = "\u001B[0m";

    // Legacy sensorLogs was an unsynchronized ArrayList written by 4 threads (race R1).
    private final Queue<String> sensorLogs = new ConcurrentLinkedQueue<>();
    private final PrintStream out;

    public ConsoleEventLogger(PrintStream out) {
        this.out = out;
    }

    @Override
    public void sensor(String message) {
        sensorLogs.add(new Date() + " : " + message);   // legacy format kept on purpose
    }

    @Override
    public void alert(String chamberId, String crop, String message) {
        out.println("[" + chamberId + "][" + crop + "][ALERT] " + message);
    }

    @Override
    public void control(String chamberId, String message) {
        out.println("\t" + GREEN + "[" + chamberId + "][CONTROL] " + message + RESET);
    }

    /** Copy of all sensor log lines. The count should always be chambers x rounds x 5. */
    public List<String> sensorLogs() {
        return List.copyOf(sensorLogs);
    }
}
