package dev.frameart.plugin.command;

import org.bukkit.command.CommandSender;

import java.util.List;

/** Um subcomando de /frameart. Novos subcomandos não exigem alterar o dispatcher (OCP). */
public interface SubCommand {

    String name();

    String permission();

    /** Chave da mensagem de uso em {@code messages.usage.*}. */
    default String usageKey() {
        return "usage." + name();
    }

    default boolean playerOnly() {
        return false;
    }

    /** {@code args} não inclui o nome do subcomando. */
    void execute(CommandSender sender, String[] args);

    default List<String> complete(CommandSender sender, String[] args) {
        return List.of();
    }
}
