package dev.frameart.core.application.usecase;

import dev.frameart.core.application.port.PlacementRepository;
import dev.frameart.core.application.port.WorldDisplay;
import dev.frameart.core.domain.FrameArtException;
import dev.frameart.core.domain.ImageId;
import dev.frameart.core.domain.ImagePlacement;

/** Remove molduras do mundo e o registro do locations.yml. Executar na thread principal. */
public final class RemoveImageUseCase {

    private final PlacementRepository repository;
    private final WorldDisplay display;

    public RemoveImageUseCase(PlacementRepository repository, WorldDisplay display) {
        this.repository = repository;
        this.display = display;
    }

    public ImagePlacement execute(ImageId id) {
        ImagePlacement placement = repository.find(id)
                .orElseThrow(() -> new FrameArtException(FrameArtException.Reason.NOT_FOUND, id.value()));
        display.remove(placement);
        repository.delete(id);
        return placement;
    }
}
