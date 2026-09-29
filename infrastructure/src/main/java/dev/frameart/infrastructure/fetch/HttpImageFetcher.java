package dev.frameart.infrastructure.fetch;

import dev.frameart.core.application.port.ImageFetcher;
import dev.frameart.core.domain.FrameArtException;
import dev.frameart.core.domain.FrameArtException.Reason;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Locale;

/** Baixa imagens via HTTP(S) com timeout e limite de tamanho. */
public final class HttpImageFetcher implements ImageFetcher {

    private final HttpClient client;
    private final Duration timeout;
    private final long maxBytes;
    private final String userAgent;
    private final boolean allowPlainHttp;

    public HttpImageFetcher(Duration timeout, long maxBytes, String userAgent, boolean allowPlainHttp) {
        this.client = HttpClient.newBuilder()
                .connectTimeout(timeout)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        this.timeout = timeout;
        this.maxBytes = maxBytes;
        this.userAgent = userAgent;
        this.allowPlainHttp = allowPlainHttp;
    }

    @Override
    public boolean supports(String source) {
        String lower = source.toLowerCase(Locale.ROOT);
        return lower.startsWith("https://") || lower.startsWith("http://");
    }

    @Override
    public byte[] fetch(String source) {
        URI uri = parse(source);
        if ("http".equalsIgnoreCase(uri.getScheme()) && !allowPlainHttp) {
            throw new FrameArtException(Reason.SOURCE_NOT_ALLOWED, "http://");
        }
        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(timeout)
                .header("User-Agent", userAgent)
                .header("Accept", "image/*")
                .GET()
                .build();
        try {
            HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
            try (InputStream body = response.body()) {
                if (response.statusCode() != 200) {
                    throw new FrameArtException(Reason.FETCH_FAILED, "HTTP " + response.statusCode());
                }
                long declared = response.headers().firstValueAsLong("Content-Length").orElse(-1L);
                if (declared > maxBytes) {
                    throw new FrameArtException(Reason.IMAGE_TOO_LARGE, (maxBytes / 1024 / 1024) + " MB");
                }
                return LimitedReader.readAll(body, maxBytes);
            }
        } catch (IOException e) {
            throw new FrameArtException(Reason.FETCH_FAILED, e.getClass().getSimpleName() + ": " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FrameArtException(Reason.FETCH_FAILED, "interrupted", e);
        }
    }

    private static URI parse(String source) {
        try {
            URI uri = URI.create(source.trim());
            if (uri.getHost() == null) {
                throw new IllegalArgumentException("missing host");
            }
            return uri;
        } catch (IllegalArgumentException e) {
            throw new FrameArtException(Reason.INVALID_SOURCE, source, e);
        }
    }
}
