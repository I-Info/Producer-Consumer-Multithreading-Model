package com.i1nfo.cst;

import java.nio.CharBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

public class Main {

    public static void main(String[] args) {
        AtomicBoolean resourceLock = new AtomicBoolean(false);
        SharedLock sharedReadLock = new SharedLock();
        CharBuffer buffer = CharBuffer.allocate(20);
        Producer producer1 = new Producer("A", resourceLock, buffer, 5);
        Producer producer2 = new Producer("B", resourceLock, buffer, 5);
        Consumer consumer1 = new Consumer("A", resourceLock, sharedReadLock, buffer, 5);
        Consumer consumer2 = new Consumer("B", resourceLock, sharedReadLock, buffer, 5);
        Consumer consumer3 = new Consumer("C", resourceLock, sharedReadLock, buffer, 5);

        new Thread(producer1).start();
        new Thread(producer2).start();
        new Thread(consumer1).start();
        new Thread(consumer2).start();
        new Thread(consumer3).start();
    }

}
