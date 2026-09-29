package dev.frameart.core.application.port;

import dev.frameart.core.domain.ImageId;
import dev.frameart.core.domain.ImagePlacement;

import java.util.Collection;
import java.util.Optional;

/** Persistência das imagens colocadas no mundo (locations.yml). */
public interface PlacementRepository {

    Optional<ImagePlacement> find(ImageId id);

    Collection<ImagePlacement> findAll();

    void save(ImagePlacement placement);

    void delete(ImageId id);

    void reload();
}
