package dev.frameart.core.application.usecase;

import dev.frameart.core.application.port.RenderCache;
import dev.frameart.core.application.port.TaskScheduler;

import java.util.concurrent.CompletableFuture;

/** Limpa os caches de memória e disco. */
public final class ClearCacheUseCase {

    private final RenderCache cache;
    private final TaskScheduler scheduler;

    public ClearCacheUseCase(RenderCache cache, TaskScheduler scheduler) {
        this.cache = cache;
        this.scheduler = scheduler;
    }

    public CompletableFuture<Void> execute() {
        return CompletableFuture.runAsync(cache::clear, scheduler.async());
    }
}
