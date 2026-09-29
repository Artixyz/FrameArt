package dev.frameart.plugin;

import dev.frameart.bukkit.version.ServerVersion;
import dev.frameart.bukkit.version.VersionAdapter;
import dev.frameart.plugin.config.PluginSettings;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class FrameArtPlugin extends JavaPlugin {

    private VersionAdapter adapter;
    private ServerVersion serverVersion;

    @Override
    public void onLoad() {
        // Servidores sem interface gráfica: garante que o AWT/ImageIO não tente abrir display.
        if (System.getProperty("java.awt.headless") == null) {
            System.setProperty("java.awt.headless", "true");
        }
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveResourceIfMissing("locations.yml");
        new File(getDataFolder(), "images").mkdirs();

        serverVersion = ServerVersion.parse(Bukkit.getBukkitVersion());
        adapter = VersionAdapterFactory.create(serverVersion);
        getLogger().info("Minecraft " + serverVersion.display() + " detected, using " + adapter.name() + " adapter.");

        start();
    }

    /** Recarrega config.yml e locations.yml, recriando todo o grafo de objetos. */
    public void reload() {
        HandlerList.unregisterAll(this);
        reloadConfig();
        start();
    }

    private void start() {
        PluginSettings settings = PluginSettings.from(getConfig());
        ApplicationContext context = ApplicationContext.create(this, settings, adapter, serverVersion, this::reload);

        PluginCommand command = getCommand("frameart");
        if (command != null) {
            command.setExecutor(context.command());
            command.setTabCompleter(context.command());
        }
        if (settings.protectFrames()) {
            getServer().getPluginManager().registerEvents(context.protectionListener(), this);
        }

        // Próximo tick: garante que todos os mundos já foram carregados.
        getServer().getScheduler().runTask(this, () -> context.restore().execute()
                .thenAccept(report -> getLogger().info(
                        "Restored " + report.restored() + "/" + report.total() + " image(s).")));
    }

    private void saveResourceIfMissing(String name) {
        if (!new File(getDataFolder(), name).exists()) {
            saveResource(name, false);
        }
    }
}
