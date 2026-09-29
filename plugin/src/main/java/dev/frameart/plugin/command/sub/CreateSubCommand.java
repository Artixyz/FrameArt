package dev.frameart.plugin.command.sub;

import dev.frameart.bukkit.world.PlacementTarget;
import dev.frameart.core.application.port.TaskScheduler;
import dev.frameart.core.application.render.GridRequest;
import dev.frameart.core.application.usecase.CreateImageRequest;
import dev.frameart.core.application.usecase.CreateImageUseCase;
import dev.frameart.core.domain.FrameArtException;
import dev.frameart.core.domain.ImageId;
import dev.frameart.plugin.command.SubCommand;
import dev.frameart.plugin.config.Messages;
import dev.frameart.plugin.config.PluginSettings;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.concurrent.CompletionException;

/** /frameart create &lt;nome&gt; &lt;url|arquivo&gt; [colunas|auto] [linhas|auto] [-dither|-nodither] */
public final class CreateSubCommand implements SubCommand {

    private final CreateImageUseCase useCase;
    private final Messages messages;
    private final TaskScheduler scheduler;
    private final PluginSettings settings;

    public CreateSubCommand(CreateImageUseCase useCase, Messages messages, TaskScheduler scheduler,
                            PluginSettings settings) {
        this.useCase = useCase;
        this.messages = messages;
        this.scheduler = scheduler;
        this.settings = settings;
    }

    @Override
    public String name() {
        return "create";
    }

    @Override
    public String permission() {
        return "frameart.create";
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            messages.send(sender, usageKey());
            return;
        }
        Player player = (Player) sender;
        Optional<PlacementTarget> target = PlacementTarget.of(player, settings.targetDistance());
        if (target.isEmpty()) {
            messages.send(sender, "look-at-wall");
            return;
        }

        CreateImageRequest request;
        try {
            request = new CreateImageRequest(
                    ImageId.of(args[0]),
                    args[1],
                    target.get().origin(),
                    target.get().facing(),
                    new GridRequest(dimension(args, 2), dimension(args, 3)),
                    dithering(args));
        } catch (FrameArtException e) {
            messages.sendError(sender, e);
            return;
        } catch (NumberFormatException e) {
            messages.send(sender, usageKey());
            return;
        }

        messages.send(sender, "processing", Map.of("name", request.id()));
        useCase.execute(request).whenCompleteAsync((placement, error) -> {
            if (error != null) {
                messages.sendError(sender, error instanceof CompletionException ? error.getCause() : error);
                return;
            }
            messages.send(sender, "created", Map.of(
                    "name", placement.id(),
                    "columns", placement.grid().columns(),
                    "rows", placement.grid().rows()));
        }, scheduler.sync());
    }

    @Override
    public List<String> complete(CommandSender sender, String[] args) {
        return switch (args.length) {
            case 2 -> List.of("https://");
            case 3, 4 -> List.of("auto", "1", "2", "3", "4");
            case 5 -> List.of("-dither", "-nodither");
            default -> List.of();
        };
    }

    private static OptionalInt dimension(String[] args, int index) {
        if (args.length <= index || args[index].startsWith("-") || args[index].equalsIgnoreCase("auto")) {
            return OptionalInt.empty();
        }
        int value = Integer.parseInt(args[index]);
        if (value < 1) {
            throw new NumberFormatException("must be positive");
        }
        return OptionalInt.of(value);
    }

    private boolean dithering(String[] args) {
        for (String arg : args) {
            if (arg.equalsIgnoreCase("-nodither")) {
                return false;
            }
            if (arg.equalsIgnoreCase("-dither")) {
                return true;
            }
        }
        return settings.dithering();
    }
}
