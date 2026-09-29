package dev.frameart.plugin.command.sub;

import dev.frameart.core.application.usecase.QueryImagesUseCase;
import dev.frameart.core.domain.ImagePlacement;
import dev.frameart.plugin.command.SubCommand;
import dev.frameart.plugin.config.Messages;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Map;

/** /frameart list */
public final class ListSubCommand implements SubCommand {

    private final QueryImagesUseCase query;
    private final Messages messages;

    public ListSubCommand(QueryImagesUseCase query, Messages messages) {
        this.query = query;
        this.messages = messages;
    }

    @Override
    public String name() {
        return "list";
    }

    @Override
    public String permission() {
        return "frameart.list";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        List<ImagePlacement> placements = query.list();
        if (placements.isEmpty()) {
            messages.send(sender, "list-empty");
            return;
        }
        messages.send(sender, "list-header", Map.of("count", placements.size()));
        for (ImagePlacement placement : placements) {
            messages.sendRaw(sender, "list-entry", Map.of(
                    "name", placement.id(),
                    "columns", placement.grid().columns(),
                    "rows", placement.grid().rows(),
                    "location", placement.area().origin()));
        }
    }
}
