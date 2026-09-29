package dev.frameart.plugin.command.sub;

import dev.frameart.plugin.command.SubCommand;
import dev.frameart.plugin.config.Messages;
import org.bukkit.command.CommandSender;

/** /frameart reload — relê config.yml e locations.yml. */
public final class ReloadSubCommand implements SubCommand {

    private final Runnable reloadAction;
    private final Messages messages;

    public ReloadSubCommand(Runnable reloadAction, Messages messages) {
        this.reloadAction = reloadAction;
        this.messages = messages;
    }

    @Override
    public String name() {
        return "reload";
    }

    @Override
    public String permission() {
        return "frameart.admin";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        reloadAction.run();
        messages.send(sender, "reloaded");
    }
}
