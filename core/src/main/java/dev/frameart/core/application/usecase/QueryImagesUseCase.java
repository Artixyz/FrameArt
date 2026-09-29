package dev.frameart.core.application.usecase;

import dev.frameart.core.application.port.PlacementRepository;
import dev.frameart.core.domain.FrameArtException;
import dev.frameart.core.domain.ImageId;
import dev.frameart.core.domain.ImagePlacement;

import java.util.Comparator;
import java.util.List;

/** Consultas somente leitura sobre as imagens registradas. */
public final class QueryImagesUseCase {

    private final PlacementRepository repository;

    public QueryImagesUseCase(PlacementRepository repository) {
        this.repository = repository;
    }

    public List<ImagePlacement> list() {
        return repository.findAll().stream()
                .sorted(Comparator.comparing(placement -> placement.id().value()))
                .toList();
    }

    public ImagePlacement get(ImageId id) {
        return repository.find(id)
                .orElseThrow(() -> new FrameArtException(FrameArtException.Reason.NOT_FOUND, id.value()));
    }
}
