package dev.frameart.plugin.command.sub;

import dev.frameart.core.application.usecase.QueryImagesUseCase;
import dev.frameart.core.application.usecase.RemoveImageUseCase;
import dev.frameart.core.domain.FrameArtException;
import dev.frameart.core.domain.ImageId;
import dev.frameart.core.domain.ImagePlacement;
import dev.frameart.plugin.command.SubCommand;
import dev.frameart.plugin.config.Messages;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Map;

/** /frameart remove &lt;nome&gt; */
public final class RemoveSubCommand implements SubCommand {

    private final RemoveImageUseCase useCase;
    private final QueryImagesUseCase query;
    private final Messages messages;

    public RemoveSubCommand(RemoveImageUseCase useCase, QueryImagesUseCase query, Messages messages) {
        this.useCase = useCase;
        this.query = query;
        this.messages = messages;
    }

    @Override
    public String name() {
        return "remove";
    }

    @Override
    public String permission() {
        return "frameart.remove";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            messages.send(sender, usageKey());
            return;
        }
        try {
            ImagePlacement removed = useCase.execute(ImageId.of(args[0]));
            messages.send(sender, "removed", Map.of("name", removed.id()));
        } catch (FrameArtException e) {
            messages.sendError(sender, e);
        }
    }

    @Override
    public List<String> complete(CommandSender sender, String[] args) {
        return ImageNames.of(query, args);
    }
}
