package dev.frameart.core.application.port;

import dev.frameart.core.application.render.RenderKey;
import dev.frameart.core.domain.RenderedImage;

import java.util.Optional;

/** Cache de imagens já processadas (tiles na paleta de mapas). */
public interface RenderCache {

    Optional<RenderedImage> get(RenderKey key);

    void put(RenderKey key, RenderedImage image);

    void clear();
}
