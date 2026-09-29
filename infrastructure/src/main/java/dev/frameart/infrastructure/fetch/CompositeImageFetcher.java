package dev.frameart.infrastructure.fetch;

import dev.frameart.core.application.port.ImageFetcher;
import dev.frameart.core.domain.FrameArtException;

import java.util.List;

/** Encaminha para o primeiro fetcher que suporta a origem. Novas origens = nova classe (OCP). */
public final class CompositeImageFetcher implements ImageFetcher {

    private final List<ImageFetcher> delegates;

    public CompositeImageFetcher(List<ImageFetcher> delegates) {
        this.delegates = List.copyOf(delegates);
    }

    @Override
    public boolean supports(String source) {
        return delegates.stream().anyMatch(fetcher -> fetcher.supports(source));
    }

    @Override
    public byte[] fetch(String source) {
        return delegates.stream()
                .filter(fetcher -> fetcher.supports(source))
                .findFirst()
                .orElseThrow(() -> new FrameArtException(FrameArtException.Reason.INVALID_SOURCE, source))
                .fetch(source);
    }
}
