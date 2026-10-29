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
public final class ControlQueue implements ControlDispatcher {

    public ControlQueue(int capacity) {
        // TODO Person B
    }

    @Override
    public void submit(ControlRequest request) throws InterruptedException {
        throw new UnsupportedOperationException("TODO Person B: queue.put(request)");
    }

    public ControlRequest take() throws InterruptedException {
        throw new UnsupportedOperationException("TODO Person B: return queue.take()");
    }
}
