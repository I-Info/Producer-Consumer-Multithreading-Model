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
                    wait(rand.nextLong(150, 155)); // Sleep random time
                }
                System.out.printf("Producer %s up\n", name);

                // Try to acquire the lock
                while (!resourceLock.compareAndSet(false, true)) {
                    // Already locked, wait for notify
                    synchronized (resourceLock) {
                        resourceLock.wait();
                    }
                }

                System.out.printf("Producer %s writing\n", name);
                // Write chars
                int length = rand.nextInt(2, 5);
                for (int i = 0; buffer.hasRemaining() && i < length; ++i) {
                    buffer.put((char) (65 + i));
                }

                System.out.println(Arrays.toString(buffer.array()));

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