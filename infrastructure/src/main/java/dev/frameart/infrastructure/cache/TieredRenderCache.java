package dev.frameart.infrastructure.cache;

import dev.frameart.core.application.port.RenderCache;
import dev.frameart.core.application.render.RenderKey;
import dev.frameart.core.domain.RenderedImage;

import java.util.Optional;

/** Cache em dois níveis (L1 memória, L2 disco). Acertos no L2 são promovidos ao L1. */
public final class TieredRenderCache implements RenderCache {

    private final RenderCache primary;
    private final RenderCache secondary;

    public TieredRenderCache(RenderCache primary, RenderCache secondary) {
        this.primary = primary;
        this.secondary = secondary;
    }

    @Override
    public Optional<RenderedImage> get(RenderKey key) {
        Optional<RenderedImage> hit = primary.get(key);
        if (hit.isPresent()) {
            return hit;
        }
        Optional<RenderedImage> fallback = secondary.get(key);
        fallback.ifPresent(image -> primary.put(key, image));
        return fallback;
    }

    @Override
    public void put(RenderKey key, RenderedImage image) {
        primary.put(key, image);
        secondary.put(key, image);
    }

    @Override
    public void clear() {
        primary.clear();
        secondary.clear();
    }
}
