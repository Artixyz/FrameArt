package dev.frameart.core.application.usecase;

import dev.frameart.core.application.port.PlacementRepository;
import dev.frameart.core.application.port.TaskScheduler;
import dev.frameart.core.application.port.WorldDisplay;
import dev.frameart.core.application.render.GridRequest;
import dev.frameart.core.application.render.ImageRenderService;
import dev.frameart.core.domain.ImagePlacement;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Reconecta as imagens salvas aos mapas após o boot/reload.
 * Graças ao cache em disco, normalmente nenhuma imagem é baixada de novo.
 */
public final class RestoreImagesUseCase {

    public record Report(int total, int restored) {
    }

    private final PlacementRepository repository;
    private final ImageRenderService renderService;
    private final WorldDisplay display;
    private final TaskScheduler scheduler;
    private final Logger logger;

    public RestoreImagesUseCase(PlacementRepository repository, ImageRenderService renderService,
                                WorldDisplay display, TaskScheduler scheduler, Logger logger) {
        this.repository = repository;
        this.renderService = renderService;
        this.display = display;
        this.scheduler = scheduler;
        this.logger = logger;
    }

    public CompletableFuture<Report> execute() {
        List<CompletableFuture<Boolean>> tasks = repository.findAll().stream().map(this::restore).toList();
        return CompletableFuture.allOf(tasks.toArray(CompletableFuture[]::new))
                .thenApply(ignored -> new Report(
                        tasks.size(),
                        (int) tasks.stream().filter(CompletableFuture::join).count()));
    }

    private CompletableFuture<Boolean> restore(ImagePlacement placement) {
        return CompletableFuture
                .supplyAsync(() -> renderService.render(
                        placement.source(), GridRequest.exact(placement.grid()), placement.dithering()),
                        scheduler.async())
                .thenApplyAsync(image -> display.attach(placement, image) == placement.mapIds().size(),
                        scheduler.sync())
                .exceptionally(error -> {
                    logger.log(Level.WARNING, "Could not restore image '" + placement.id() + "'", error);
                    return false;
                });
    }
}
