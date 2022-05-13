package com.i1nfo.cst;

import java.nio.CharBuffer;
import java.util.Arrays;
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
                    int position;
//                    synchronized (buffer) {
                    position = buffer.position();
//                    }
                    if (position == 0) {
                        // Empty, wait for writing thread
                        synchronized (resourceLock) {
                            resourceLock.wait();
                        }
                        position = buffer.position();
                    }
                    if (position == 1) {
                        sharedLock.setReadUnavailableSignal(true);
                    }
                    System.out.printf("Max read count: %s\n", position);
                    sharedLock.setMaxCount(position);
                    // Try to acquire resource lock
                    while (!resourceLock.compareAndSet(false, true)) {
                        // Already locked, wait for notify
                        synchronized (resourceLock) {
                            resourceLock.wait();
                        }
                    }
                    buffer.flip();
                    // Resource locked by shared read lock, notify other read thread.
                    sharedLock.sNotify();
                }

                // Read operation...
                System.out.printf("Consumer %s reading..\n", name);
                synchronized (buffer) {
                    System.out.printf("Consumer %s get: %s\n", name, buffer.get());
                    System.out.printf("%s %s %s\n", Arrays.toString(buffer.array()), buffer.position(), buffer.limit());
                }
                System.out.printf("Consumer %s read finish..\n", name);

                if (sharedLock.decrease()) {
                    // The last read thread, release the resource lock.
                    buffer.compact();
                    resourceLock.set(false);
                    synchronized (resourceLock) {
                        resourceLock.notify();
                    }
                    sharedLock.sNotify();
                }
                System.out.printf("Consumer %s down\n", name);

            }
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }

}
