package com.i1nfo.cst;

import java.util.concurrent.atomic.AtomicBoolean;

public class SharedLock {
    private volatile int count;

    private volatile int maxCount;

    private final AtomicBoolean readUnavailableSignal;

    private final AtomicBoolean lock;

    SharedLock() {
        count = 0;
        lock = new AtomicBoolean(false);
        readUnavailableSignal = new AtomicBoolean(false);
    }

    public boolean increase() throws InterruptedException {
        // Acquire lock
        int spinCount = 0;
        while (readUnavailableSignal.get() || !lock.compareAndSet(false, true)) {
            // Already locked, waiting.
            if (++spinCount == 10) {
                synchronized (this) {
                    wait();
                }
            }
        }

        if (++count == 1) {
            return true;
        } else {
            if (count == maxCount) {
                readUnavailableSignal.set(true);
            }
            sNotify();
            return false;
        }
    }

    public boolean decrease() throws InterruptedException {
        // Acquire lock
        int spinCount = 0;
        while (!lock.compareAndSet(false, true)) {
            // Already locked, waiting.
            if (++spinCount == 10) {
                synchronized (this) {
                    wait();
                }
            }
        }

        if (--count == 0) {
            readUnavailableSignal.set(false);
            return true;
        } else {
            sNotify();
            return false;
        }
    }

    public void sNotify() {
        // release lock
        lock.set(false);
        synchronized (this) {
            notify();
        }
    }

    public void setMaxCount(int maxCount) {
        this.maxCount = maxCount;
    }

    public void setReadUnavailableSignal(boolean flag) {
        readUnavailableSignal.set(flag);
    }
}
