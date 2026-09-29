package dev.frameart.core.application.usecase;

import dev.frameart.core.application.render.GridRequest;
import dev.frameart.core.domain.BlockPosition;
import dev.frameart.core.domain.Facing;
import dev.frameart.core.domain.ImageId;

/** Dados de entrada para criar uma imagem na parede. */
public record CreateImageRequest(
        ImageId id,
        String source,
        BlockPosition origin,
        Facing facing,
        GridRequest grid,
        boolean dithering) {
}
