package com.warehouse.util;

import java.util.Scanner;
import java.util.ArrayDeque;
import java.util.Deque;

public final class ConsoleIO {
    private static final Object LOCK = new Object();
    private static final Scanner SCANNER = new Scanner(System.in);
    private static final Deque<String> PENDING_LINES = new ArrayDeque<>();

    private ConsoleIO() {
    }

    public static String readLine(String prompt) {
        synchronized (LOCK) {
            boolean flushed = flushPendingLinesUnsafe();
            if (flushed) {
                System.out.println();
            }
            System.out.print(prompt);
            return SCANNER.nextLine();
        }
    }

    public static void println(String message) {
        synchronized (LOCK) {
            System.out.println(message);
        }
    }

    public static void enqueueLine(String message) {
        synchronized (LOCK) {
            PENDING_LINES.addLast(message);
        }
    }

    public static void flushPendingLines() {
        synchronized (LOCK) {
            flushPendingLinesUnsafe();
        }
    }

    private static boolean flushPendingLinesUnsafe() {
        boolean flushed = false;
        while (!PENDING_LINES.isEmpty()) {
            System.out.println(PENDING_LINES.removeFirst());
            flushed = true;
        }
        return flushed;
    }
}

