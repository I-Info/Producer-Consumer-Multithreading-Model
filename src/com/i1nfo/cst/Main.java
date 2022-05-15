package com.i1nfo.cst;

import java.nio.CharBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

public class Main {

    public static void main(String[] args) {
        AtomicBoolean resourceLock = new AtomicBoolean(false);
        SharedLock sharedReadLock = new SharedLock();
        CharBuffer buffer = CharBuffer.allocate(20);
        for (int i = 0; i < 5; ++i) {
            new Thread(new Producer("P" + (char) (65 + i), resourceLock, buffer, 15)).start();
        }
        for (int i = 0; i < 10; ++i) {
            new Thread(new Consumer("C" + (char) (65 + i), resourceLock, sharedReadLock, buffer, 20)).start();
        }

    }

}
