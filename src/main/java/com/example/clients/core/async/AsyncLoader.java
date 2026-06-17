package com.example.clients.core.async;

import javafx.concurrent.Task;

import java.util.function.Consumer;
import java.util.function.Supplier;

public final class AsyncLoader {

    private AsyncLoader() {
    }

    public static <T> Task<T> run(
            Supplier<T> backgroundWork,
            Consumer<T> onSuccess,
            Consumer<Throwable> onError
    ) {
        Task<T> task = new Task<>() {
            @Override
            protected T call() {
                return backgroundWork.get();
            }
        };

        task.setOnSucceeded(event -> onSuccess.accept(task.getValue()));
        task.setOnFailed(event -> onError.accept(task.getException()));

        BackgroundExecutor.executor().submit(task);
        return task;
    }
}
