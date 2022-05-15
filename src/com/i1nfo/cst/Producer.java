package com.i1nfo.cst;

import java.nio.CharBuffer;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class Producer implements Runnable {

    private final AtomicBoolean resourceLock;
    private final CharBuffer buffer;

    private final String name;

    private final int runTimes;

    Producer(String name, AtomicBoolean lock, CharBuffer buffer, int runTimes) {
        this.resourceLock = lock;
        this.buffer = buffer;
        this.name = name;
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
                System.out.printf("Producer %s up\n", name);

                // Try to acquire the lock
                int spinCount = 0;
                while (!resourceLock.compareAndSet(false, true)) {
                    // Already locked, wait for notify
                    if (++spinCount == 10) {
                        synchronized (resourceLock) {
                            resourceLock.wait();
                        }
                        spinCount = 0;
                    }
                }

                System.out.printf("Producer %s writing..\n", name);
                // Write chars
                int length = rand.nextInt(2, 8);
                for (int i = 0; buffer.hasRemaining() && i < length; ++i) {
                    buffer.put((char) (65 + i));
                }

                System.out.printf("%s [position: %s, limit: %s]\n", Arrays.toString(buffer.array()), buffer.position(), buffer.limit());
                System.out.printf("Producer %s write finish..\n", name);
                // Release the lock
                resourceLock.set(false);
                synchronized (resourceLock) {
                    resourceLock.notify();
                }
                System.out.printf("Producer %s down\n", name);
            }
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }
}
