package dev.frameart.core.application.usecase;

import dev.frameart.core.application.port.PlacementRepository;
import dev.frameart.core.application.port.TaskScheduler;
import dev.frameart.core.application.port.WorldDisplay;
import dev.frameart.core.application.render.ImageRenderService;
import dev.frameart.core.domain.FrameArtException;
import dev.frameart.core.domain.ImageId;
import dev.frameart.core.domain.ImagePlacement;
import dev.frameart.core.domain.PlacementArea;
import dev.frameart.core.domain.RenderedImage;

import java.time.Clock;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/** Baixa/processa a imagem de forma assíncrona e a coloca no mundo na thread principal. */
public final class CreateImageUseCase {

    private final ImageRenderService renderService;
    private final WorldDisplay display;
    private final PlacementRepository repository;
    private final TaskScheduler scheduler;
    private final Clock clock;

    public CreateImageUseCase(ImageRenderService renderService, WorldDisplay display,
                              PlacementRepository repository, TaskScheduler scheduler, Clock clock) {
        this.renderService = renderService;
        this.display = display;
        this.repository = repository;
        this.scheduler = scheduler;
        this.clock = clock;
    }

    public CompletableFuture<ImagePlacement> execute(CreateImageRequest request) {
        try {
            ensureAvailable(request.id());
        } catch (FrameArtException e) {
            return CompletableFuture.failedFuture(e);
        }
        return CompletableFuture
                .supplyAsync(() -> renderService.render(request.source(), request.grid(), request.dithering()),
                        scheduler.async())
                .thenApplyAsync(image -> place(request, image), scheduler.sync());
    }

    private ImagePlacement place(CreateImageRequest request, RenderedImage image) {
        // Revalida: outro comando pode ter usado o mesmo nome enquanto baixávamos.
        ensureAvailable(request.id());
        PlacementArea area = new PlacementArea(request.origin(), request.facing(), image.grid());
        List<Integer> mapIds = display.place(area, image);
        ImagePlacement placement = new ImagePlacement(
                request.id(), request.source(), area, request.dithering(), mapIds, clock.instant());
        repository.save(placement);
        return placement;
    }

    private void ensureAvailable(ImageId id) {
        if (repository.find(id).isPresent()) {
            throw new FrameArtException(FrameArtException.Reason.NAME_TAKEN, id.value());
        }
    }
}
