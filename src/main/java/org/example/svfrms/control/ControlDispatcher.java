package org.example.svfrms.control;

/** Where SensorProcessor sends device requests (DIP: it doesn't know a queue exists). */
@FunctionalInterface
public interface ControlDispatcher {
    void submit(ControlRequest request) throws InterruptedException;
}
