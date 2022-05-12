package com.i1nfo.cst;

import java.nio.CharBuffer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {

    public static void main(String[] args) {
        AtomicBoolean resourceLock = new AtomicBoolean(false);
        SharedLock sharedReadLock = new SharedLock();
        CharBuffer buffer = CharBuffer.allocate(20);
        Producer producer1 = new Producer("A", resourceLock, buffer, 5);
        Producer producer2 = new Producer("B", resourceLock, buffer, 5);
        new Thread(producer1).start();
        new Thread(producer2).start();
    }

}
