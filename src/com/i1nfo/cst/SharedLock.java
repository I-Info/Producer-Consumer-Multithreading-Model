package com.i1nfo.cst;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class SharedLock {
    private int count;
    private final AtomicBoolean lock;

    SharedLock() {
        count = 0;
        lock = new AtomicBoolean(false);
    }

    public boolean increase() throws InterruptedException {
        // Acquire lock
        if (!lock.compareAndSet(false, true)) {
            // Already locked, waiting.
            synchronized (this) {
                wait();
            }
        }

        return ++count == 1;
    }

    public boolean decrease() throws InterruptedException {
        // Acquire lock
        if (!lock.compareAndSet(false, true)) {
            // Already locked, waiting.
            synchronized (this) {
                wait();
            }
        }

        return --count == 0;
    }


}
