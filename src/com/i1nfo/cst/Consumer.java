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
                    wait(rand.nextLong(30, 31)); // Sleep random time
                }
                System.out.printf("Consumer %s up\n", name);

                // Get shared lock
                int index;
                if ((index = sharedLock.increase() - 1) == 0) {
                    int position;
                    position = buffer.position();
                    if (position == 0) {
                        // Empty, wait for writing thread
                        synchronized (resourceLock) {
                            resourceLock.wait();
                        }
                        position = buffer.position();
                    }
                    if (position == 1) {
                        // Only one available read count
                        sharedLock.setReadUnavailableSignal();
                    }
                    sharedLock.setMaxReadCount(position);
                    System.out.println("Max read count: " + position);
                    // Try to acquire resource lock
                    int spinCount = 0;
                    while (!resourceLock.compareAndSet(false, true)) {
                        // Already locked, wait for notify
                        if (++spinCount == 10) {
                            synchronized (resourceLock) {
                                resourceLock.wait();
                            }
                        }
                    }
                    buffer.flip();
                    // Resource locked by shared read lock, notify other read thread.
                    sharedLock.sNotify();
                }

                // Read operation...
                System.out.printf("Consumer %s reading..\n", name);
                if (index > buffer.length()) {
                    System.out.println("err");
                }
                System.out.printf("Consumer %s get: '%s' from %s [index: %s, limit: %s]\n", name, buffer.get(index), Arrays.toString(buffer.array()), index, buffer.limit() - 1);
                System.out.printf("Consumer %s read finish..\n", name);

                if (sharedLock.decrease()) {
                    // The last read thread, release the resource lock.
                    int readCount = sharedLock.getReadCount();
                    System.out.printf("Total read: %s\n", readCount);
                    buffer.position(readCount);
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
