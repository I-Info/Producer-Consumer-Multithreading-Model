package com.i1nfo.cst;

import java.nio.CharBuffer;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class Consumer implements Runnable {

    private final AtomicBoolean resourceLock;

    private final SharedLock sharedLock;

    private final CharBuffer buffer;

    private final String name;

    private final int runTimes;

    public Consumer(String name, AtomicBoolean lock, SharedLock sLock, CharBuffer buffer, int runTimes) {
        this.name = name;
        this.resourceLock = lock;
        this.sharedLock = sLock;
        this.buffer = buffer;
        this.runTimes = runTimes;
    }

    @Override
    public void run() {
        try {
            Random rand = new Random();
            for (int j = 0; j < runTimes; ++j) {
                // Main event loop
                synchronized (this) {
                    wait(rand.nextLong(50, 55)); // Sleep random time
                }
                System.out.printf("Consumer %s up\n", name);

                // Get shared lock
                if (sharedLock.increase()) {
                    // TODO: avoid useless reading
                    if (buffer.position() == 0) {
                       // Empty, wait for writing thread
                       resourceLock.wait();
                    }
                    // Try to acquire resource lock
                    while (!resourceLock.compareAndSet(false, true)) {
                        // Already locked, wait for notify
                        synchronized (resourceLock) {
                            resourceLock.wait();
                        }
                    }
                    buffer.flip();
                    // Resource locked by shared read lock, notify other read thread.
                    sharedLock.notify();
                }

                // Read operation...
                System.out.printf("Consumer %s reading\n", name);


                if (sharedLock.decrease()) {
                    // The last read thread, release the resource lock.
                    buffer.compact();
                    resourceLock.set(false);
                    synchronized (resourceLock) {
                        resourceLock.notify();
                    }
                    sharedLock.notify();
                }

            }
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }

}
