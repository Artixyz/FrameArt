package dev.frameart.plugin.command;

import dev.frameart.plugin.config.Messages;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Dispatcher de /frameart &lt;subcomando&gt;. */
public final class FrameArtCommand implements CommandExecutor, TabCompleter {

    private final Messages messages;
    private final Map<String, SubCommand> subCommands = new LinkedHashMap<>();

    public FrameArtCommand(Messages messages, List<SubCommand> subCommands) {
        this.messages = messages;
        subCommands.forEach(sub -> this.subCommands.put(sub.name(), sub));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        SubCommand sub = args.length == 0 ? null : subCommands.get(args[0].toLowerCase(Locale.ROOT));
        if (sub == null) {
            sendHelp(sender);
            return true;
        }
        if (!sender.hasPermission(sub.permission())) {
            messages.send(sender, "no-permission");
            return true;
        }
        if (sub.playerOnly() && !(sender instanceof Player)) {
            messages.send(sender, "player-only");
            return true;
        }
        sub.execute(sender, Arrays.copyOfRange(args, 1, args.length));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> names = subCommands.values().stream()
                    .filter(sub -> sender.hasPermission(sub.permission()))
                    .map(SubCommand::name)
                    .toList();
            return StringUtil.copyPartialMatches(args[0], names, new ArrayList<>());
        }
        SubCommand sub = subCommands.get(args[0].toLowerCase(Locale.ROOT));
        if (sub == null || !sender.hasPermission(sub.permission())) {
            return List.of();
        }
        String[] rest = Arrays.copyOfRange(args, 1, args.length);
        return StringUtil.copyPartialMatches(rest[rest.length - 1], sub.complete(sender, rest), new ArrayList<>());
    }

    private void sendHelp(CommandSender sender) {
        messages.sendRaw(sender, "help-header", Map.of());
        for (SubCommand sub : subCommands.values()) {
            if (sender.hasPermission(sub.permission())) {
                messages.sendRaw(sender, sub.usageKey(), Map.of());
            }
        }
    }
}
