package com.example.clients.core.async;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class BackgroundExecutor {

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "clients-background-worker");
        thread.setDaemon(true);
        return thread;
    });

    private BackgroundExecutor() {
    }

    static ExecutorService executor() {
        return EXECUTOR;
    }

    public static void shutdown() {
        EXECUTOR.shutdownNow();
    }
}
