package org.example.svfrms.logging;

/**
 * Output side of the system, replacing the LOG / CREATE_ALERT print / CONTROL_LOG branches of
 * legacy handleEvent (lines 62-88). Business logic calls this interface and never System.out directly.
 */
public interface EventLogger {

    /** Sensor processing trace (legacy "LOG": stored, never printed). */
    void sensor(String message);

    /** "[CH1][Tomato][ALERT] message" */
    void alert(String chamberId, String crop, String message);

    /** Green "\t[CH1][CONTROL] message" */
    void control(String chamberId, String message);
}
