package dev.frameart.plugin.config;

import dev.frameart.core.domain.FrameArtException;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;

import java.util.Map;

/** Mensagens configuráveis (seção {@code messages} do config.yml) com placeholders {chave}. */
public final class Messages {

    private final ConfigurationSection section;
    private final String prefix;

    public Messages(ConfigurationSection section) {
        this.section = section == null ? new MemoryConfiguration() : section;
        this.prefix = color(this.section.getString("prefix", ""));
    }

    public String format(String key, Map<String, ?> placeholders) {
        String text = section.getString(key, key);
        for (Map.Entry<String, ?> entry : placeholders.entrySet()) {
            text = text.replace("{" + entry.getKey() + "}", String.valueOf(entry.getValue()));
        }
        return color(text);
    }

    public void send(CommandSender sender, String key) {
        send(sender, key, Map.of());
    }

    public void send(CommandSender sender, String key, Map<String, ?> placeholders) {
        sender.sendMessage(prefix + format(key, placeholders));
    }

    /** Linha sem prefixo (listas, cabeçalhos). */
    public void sendRaw(CommandSender sender, String key, Map<String, ?> placeholders) {
        sender.sendMessage(format(key, placeholders));
    }

    public void sendError(CommandSender sender, Throwable error) {
        if (error instanceof FrameArtException frameArt) {
            send(sender, "errors." + frameArt.reason().key(), Map.of("detail", frameArt.detail()));
        } else {
            send(sender, "errors.unexpected", Map.of("detail", String.valueOf(error.getMessage())));
        }
    }

    private static String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
