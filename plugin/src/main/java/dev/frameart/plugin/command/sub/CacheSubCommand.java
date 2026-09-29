package dev.frameart.plugin.command.sub;

import dev.frameart.core.application.port.TaskScheduler;
import dev.frameart.core.application.usecase.ClearCacheUseCase;
import dev.frameart.plugin.command.SubCommand;
import dev.frameart.plugin.config.Messages;
import org.bukkit.command.CommandSender;

import java.util.List;

/** /frameart cache clear */
public final class CacheSubCommand implements SubCommand {

    private final ClearCacheUseCase clearCache;
    private final Messages messages;
    private final TaskScheduler scheduler;

    public CacheSubCommand(ClearCacheUseCase clearCache, Messages messages, TaskScheduler scheduler) {
        this.clearCache = clearCache;
        this.messages = messages;
        this.scheduler = scheduler;
    }

    @Override
    public String name() {
        return "cache";
    }

    @Override
    public String permission() {
        return "frameart.admin";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 1 || !args[0].equalsIgnoreCase("clear")) {
            messages.send(sender, usageKey());
            return;
        }
        clearCache.execute().whenCompleteAsync((ignored, error) -> {
            if (error != null) {
                messages.sendError(sender, error);
            } else {
                messages.send(sender, "cache-cleared");
            }
        }, scheduler.sync());
    }

    @Override
    public List<String> complete(CommandSender sender, String[] args) {
        return args.length == 1 ? List.of("clear") : List.of();
    }
}
