package dev.frameart.plugin;

import dev.frameart.bukkit.listener.FrameProtectionListener;
import dev.frameart.bukkit.palette.BukkitColorPalette;
import dev.frameart.bukkit.persistence.YamlPlacementRepository;
import dev.frameart.bukkit.scheduler.BukkitTaskScheduler;
import dev.frameart.bukkit.version.FrameOptions;
import dev.frameart.bukkit.version.ServerVersion;
import dev.frameart.bukkit.version.VersionAdapter;
import dev.frameart.bukkit.world.BukkitWorldDisplay;
import dev.frameart.core.application.port.RenderCache;
import dev.frameart.core.application.port.TaskScheduler;
import dev.frameart.core.application.port.WorldDisplay;
import dev.frameart.core.application.render.ImageProcessor;
import dev.frameart.core.application.render.ImageRenderService;
import dev.frameart.core.application.render.SizingPolicy;
import dev.frameart.core.application.usecase.ClearCacheUseCase;
import dev.frameart.core.application.usecase.CreateImageUseCase;
import dev.frameart.core.application.usecase.QueryImagesUseCase;
import dev.frameart.core.application.usecase.RemoveImageUseCase;
import dev.frameart.core.application.usecase.RestoreImagesUseCase;
import dev.frameart.infrastructure.cache.DiskRenderCache;
import dev.frameart.infrastructure.cache.MemoryRenderCache;
import dev.frameart.infrastructure.cache.TieredRenderCache;
import dev.frameart.infrastructure.decode.ImageIoDecoder;
import dev.frameart.infrastructure.fetch.CompositeImageFetcher;
import dev.frameart.infrastructure.fetch.HttpImageFetcher;
import dev.frameart.infrastructure.fetch.LocalFileImageFetcher;
import dev.frameart.plugin.command.FrameArtCommand;
import dev.frameart.plugin.command.sub.CacheSubCommand;
import dev.frameart.plugin.command.sub.CreateSubCommand;
import dev.frameart.plugin.command.sub.InfoSubCommand;
import dev.frameart.plugin.command.sub.ListSubCommand;
import dev.frameart.plugin.command.sub.ReloadSubCommand;
import dev.frameart.plugin.command.sub.RemoveSubCommand;
import dev.frameart.plugin.config.Messages;
import dev.frameart.plugin.config.PluginSettings;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.nio.file.Path;
import java.time.Clock;
import java.util.List;
import java.util.logging.Logger;

/**
 * Composition root: o único lugar que conhece as implementações concretas.
 * Todo o resto depende de abstrações (DIP).
 */
final class ApplicationContext {

    private final RestoreImagesUseCase restore;
    private final FrameArtCommand command;
    private final FrameProtectionListener protectionListener;

    private ApplicationContext(RestoreImagesUseCase restore, FrameArtCommand command,
                               FrameProtectionListener protectionListener) {
        this.restore = restore;
        this.command = command;
        this.protectionListener = protectionListener;
    }

    static ApplicationContext create(JavaPlugin plugin, PluginSettings settings, VersionAdapter adapter,
                                     ServerVersion version, Runnable reloadAction) {
        Logger logger = plugin.getLogger();
        Path dataFolder = plugin.getDataFolder().toPath();

        TaskScheduler scheduler = new BukkitTaskScheduler(plugin);
        YamlPlacementRepository repository =
                new YamlPlacementRepository(new File(plugin.getDataFolder(), "locations.yml"), logger);
        WorldDisplay display = new BukkitWorldDisplay(
                adapter, new FrameOptions(settings.invisibleFrames(), settings.fixedFrames()), logger);

        RenderCache memory = new MemoryRenderCache(settings.memoryCacheEntries());
        RenderCache cache = settings.diskCache()
                ? new TieredRenderCache(memory, new DiskRenderCache(dataFolder.resolve("cache"), logger))
                : memory;

        ImageRenderService renderService = new ImageRenderService(
                new CompositeImageFetcher(List.of(
                        new HttpImageFetcher(settings.downloadTimeout(), settings.maxDownloadBytes(),
                                settings.userAgent(), settings.allowPlainHttp()),
                        new LocalFileImageFetcher(dataFolder.resolve("images"), settings.maxDownloadBytes()))),
                new ImageIoDecoder(settings.maxImageDimension()),
                new ImageProcessor(new BukkitColorPalette(version.raw())),
                new SizingPolicy(settings.maxColumns(), settings.maxRows()),
                cache);

        Messages messages = new Messages(plugin.getConfig().getConfigurationSection("messages"));
        QueryImagesUseCase query = new QueryImagesUseCase(repository);

        FrameArtCommand command = new FrameArtCommand(messages, List.of(
                new CreateSubCommand(
                        new CreateImageUseCase(renderService, display, repository, scheduler, Clock.systemUTC()),
                        messages, scheduler, settings),
                new RemoveSubCommand(new RemoveImageUseCase(repository, display), query, messages),
                new ListSubCommand(query, messages),
                new InfoSubCommand(query, messages),
                new CacheSubCommand(new ClearCacheUseCase(cache, scheduler), messages, scheduler),
                new ReloadSubCommand(reloadAction, messages)));

        return new ApplicationContext(
                new RestoreImagesUseCase(repository, renderService, display, scheduler, logger),
                command,
                new FrameProtectionListener(repository, adapter));
    }

    RestoreImagesUseCase restore() {
        return restore;
    }

    FrameArtCommand command() {
        return command;
    }

    FrameProtectionListener protectionListener() {
        return protectionListener;
    }
}
