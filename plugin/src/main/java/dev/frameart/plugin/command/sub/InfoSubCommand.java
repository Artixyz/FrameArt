package dev.frameart.plugin.command.sub;

import dev.frameart.core.application.usecase.QueryImagesUseCase;
import dev.frameart.core.domain.FrameArtException;
import dev.frameart.core.domain.ImageId;
import dev.frameart.core.domain.ImagePlacement;
import dev.frameart.plugin.command.SubCommand;
import dev.frameart.plugin.config.Messages;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Map;

/** /frameart info &lt;nome&gt; */
public final class InfoSubCommand implements SubCommand {

    private final QueryImagesUseCase query;
    private final Messages messages;

    public InfoSubCommand(QueryImagesUseCase query, Messages messages) {
        this.query = query;
        this.messages = messages;
    }

    @Override
    public String name() {
        return "info";
    }

    @Override
    public String permission() {
        return "frameart.list";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            messages.send(sender, usageKey());
            return;
        }
        try {
            ImagePlacement placement = query.get(ImageId.of(args[0]));
            messages.sendRaw(sender, "info", Map.of(
                    "name", placement.id(),
                    "source", placement.source(),
                    "location", placement.area().origin(),
                    "facing", placement.area().facing(),
                    "columns", placement.grid().columns(),
                    "rows", placement.grid().rows(),
                    "dithering", placement.dithering(),
                    "maps", placement.mapIds()));
        } catch (FrameArtException e) {
            messages.sendError(sender, e);
        }
    }

    @Override
    public List<String> complete(CommandSender sender, String[] args) {
        return ImageNames.of(query, args);
    }
}
