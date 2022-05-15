package com.i1nfo.cst;

import java.util.concurrent.atomic.AtomicInteger;

public class SharedLock {
    private volatile int count;

    private volatile int maxReadCount;

    private volatile int readCount;

    private final AtomicInteger lock;


    SharedLock() {
        count = 0;
        readCount = 0;
        lock = new AtomicInteger(0);
    }

    public int increase() throws InterruptedException {
        // Acquire lock
        int spinCount = 0;
        while (!lock.compareAndSet(0, 1)) {
            // Already locked, waiting.
            if (++spinCount == 10) {
                synchronized (this) {
                    notify();
                    wait();
                }
                spinCount = 0;
            }
        }

        if (++count == 1) {
            // Init read count
            readCount = 1;
        } else {
            ++readCount;
            if (readCount == maxReadCount) {
                lock.set(3);
            }
            sNotify();
        }

        return readCount;
    }

    public boolean decrease() throws InterruptedException {
        // Acquire lock
        int spinCount = 0;
        while (!lock.compareAndSet(0, 1) && !lock.compareAndSet(2, 3)) {
            // Already locked, waiting.
            if (++spinCount == 10) {
                synchronized (this) {
                    wait();
                }
                spinCount = 0;
            }
        }

        if (--count == 0) {
            lock.set(1);
            return true;
        } else {
            sNotify();
            return false;
        }
    }

    public void sNotify() {
        // release lock
        if (lock.get() == 1) {
            lock.set(0);
        } else if (lock.get() == 3) {
            lock.set(2);
        }
        synchronized (this) {
            notifyAll();
        }
    }

    public void setMaxReadCount(int maxReadCount) {
        this.maxReadCount = maxReadCount;
    }

    public void setReadUnavailableSignal() {
        lock.set(3);
    }

    public int getReadCount() {
        return readCount;
    }
}
