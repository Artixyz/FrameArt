package dev.frameart.core.application.render;

import dev.frameart.core.application.port.ImageDecoder;
import dev.frameart.core.application.port.ImageFetcher;
import dev.frameart.core.application.port.RenderCache;
import dev.frameart.core.domain.RenderedImage;
import dev.frameart.core.domain.TileGrid;

import java.awt.image.BufferedImage;
import java.util.Optional;

/**
 * Transforma uma origem em {@link RenderedImage}, consultando o cache antes de baixar/processar.
 * Quando o tamanho é exato (ex.: restauração no boot), um acerto de cache evita qualquer download.
 */
public final class ImageRenderService {

    private final ImageFetcher fetcher;
    private final ImageDecoder decoder;
    private final ImageProcessor processor;
    private final SizingPolicy sizing;
    private final RenderCache cache;

    public ImageRenderService(ImageFetcher fetcher, ImageDecoder decoder, ImageProcessor processor,
                              SizingPolicy sizing, RenderCache cache) {
        this.fetcher = fetcher;
        this.decoder = decoder;
        this.processor = processor;
        this.sizing = sizing;
        this.cache = cache;
    }

    /** Operação bloqueante: executar fora da thread principal. */
    public RenderedImage render(String source, GridRequest request, boolean dithering) {
        if (request.isExact()) {
            Optional<RenderedImage> cached = cache.get(keyOf(source, request.toGrid(), dithering));
            if (cached.isPresent()) {
                return cached.get();
            }
        }
        BufferedImage image = decoder.decode(fetcher.fetch(source));
        TileGrid grid = sizing.resolve(request, image.getWidth(), image.getHeight());
        RenderKey key = keyOf(source, grid, dithering);
        return cache.get(key).orElseGet(() -> {
            RenderedImage rendered = processor.process(image, grid, dithering);
            cache.put(key, rendered);
            return rendered;
        });
    }

    private RenderKey keyOf(String source, TileGrid grid, boolean dithering) {
        return new RenderKey(source, grid, dithering, processor.paletteId());
    }
}
