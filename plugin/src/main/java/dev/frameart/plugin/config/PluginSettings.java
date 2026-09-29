package dev.frameart.plugin.config;

import org.bukkit.configuration.file.FileConfiguration;

import java.time.Duration;

/** Snapshot imutável do config.yml. */
public record PluginSettings(
        int maxColumns,
        int maxRows,
        boolean dithering,
        int maxImageDimension,
        int targetDistance,
        Duration downloadTimeout,
        long maxDownloadBytes,
        String userAgent,
        boolean allowPlainHttp,
        boolean invisibleFrames,
        boolean fixedFrames,
        boolean protectFrames,
        int memoryCacheEntries,
        boolean diskCache) {

    public static PluginSettings from(FileConfiguration config) {
        return new PluginSettings(
                positive(config.getInt("images.max-columns", 10)),
                positive(config.getInt("images.max-rows", 10)),
                config.getBoolean("images.dithering", true),
                positive(config.getInt("images.max-source-dimension", 8192)),
                positive(config.getInt("images.target-distance", 10)),
                Duration.ofSeconds(positive(config.getInt("download.timeout-seconds", 10))),
                positive(config.getInt("download.max-size-mb", 10)) * 1024L * 1024L,
                config.getString("download.user-agent", "FrameArt/1.0"),
                config.getBoolean("download.allow-http", true),
                config.getBoolean("frames.invisible", false),
                config.getBoolean("frames.fixed", true),
                config.getBoolean("frames.protect", true),
                positive(config.getInt("cache.memory-entries", 64)),
                config.getBoolean("cache.disk", true));
    }

    private static int positive(int value) {
        return Math.max(1, value);
    }
}
