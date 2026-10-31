package org.example.svfrms.control;

/**
 * TODO (Person B - concurrency): bounded producer-consumer queue.
 *
 * Replaces the hand-written wait/notifyAll queue in legacy addControlRequest (lines 131-139)
 * and ControlThread (lines 256-267). Suggested fix: wrap a
 * java.util.concurrent.ArrayBlockingQueue<ControlRequest>(capacity):
 *   submit -> put()  (blocks when full, like legacy)
 *   take   -> take() (blocks when empty)
 * Do NOT catch and ignore InterruptedException (legacy lines 134, 258) - let it propagate.
 */
package org.example.svfrms.control;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * Bounded producer-consumer queue.
 *
 * Replaces the hand-written wait/notifyAll queue in legacy addControlRequest (lines 131-139)
 * and ControlThread (lines 256-267). 
 */
public final class ControlQueue implements ControlDispatcher {

    // ArrayBlockingQueue is inherently thread-safe and handles all blocking/waiting logic.
    private final ArrayBlockingQueue<ControlRequest> queue;

    public ControlQueue(int capacity) {
        // Initialize the bounded queue with the specified capacity (e.g., 5, matching legacy)
        this.queue = new ArrayBlockingQueue<>(capacity);
    }

    @Override
    public void submit(ControlRequest request) throws InterruptedException {
        // put() automatically blocks the calling thread if the queue is full.
        // It throws InterruptedException if the thread is interrupted while waiting,
        // which we properly propagate instead of swallowing like the legacy code.
        queue.put(request);
    }

    public ControlRequest take() throws InterruptedException {
        // take() automatically blocks the calling thread if the queue is empty.
        // It wakes up and returns the request as soon as a producer adds one.
        return queue.take();
    }
}