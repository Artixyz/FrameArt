package dev.frameart.bukkit.scheduler;

import dev.frameart.core.application.port.TaskScheduler;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.Executor;

/** Executors baseados no scheduler do Bukkit (disponível de 1.8 a 26.x). */
public final class BukkitTaskScheduler implements TaskScheduler {

    private final Executor async;
    private final Executor sync;

    public BukkitTaskScheduler(Plugin plugin) {
        this.async = task -> Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
        this.sync = task -> {
            if (Bukkit.isPrimaryThread()) {
                task.run();
            } else {
                Bukkit.getScheduler().runTask(plugin, task);
            }
        };
    }

    @Override
    public Executor async() {
        return async;
    }

    @Override
    public Executor sync() {
        return sync;
    }
}
