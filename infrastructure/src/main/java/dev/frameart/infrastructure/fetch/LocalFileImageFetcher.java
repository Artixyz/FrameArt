package dev.frameart.infrastructure.fetch;

import dev.frameart.core.application.port.ImageFetcher;
import dev.frameart.core.domain.FrameArtException;
import dev.frameart.core.domain.FrameArtException.Reason;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

/**
 * Lê imagens da pasta {@code plugins/FrameArt/images}. Aceita "logo.png" ou "file:logo.png".
 * Bloqueia path traversal ("../") para fora da pasta.
 */
public final class LocalFileImageFetcher implements ImageFetcher {

    private static final String PREFIX = "file:";

    private final Path directory;
    private final long maxBytes;

    public LocalFileImageFetcher(Path directory, long maxBytes) {
        this.directory = directory.toAbsolutePath().normalize();
        this.maxBytes = maxBytes;
    }

    @Override
    public boolean supports(String source) {
        return !source.contains("://");
    }

    @Override
    public byte[] fetch(String source) {
        String name = source.startsWith(PREFIX) ? source.substring(PREFIX.length()) : source;
        Path file = resolve(name);
        if (!Files.isRegularFile(file)) {
            throw new FrameArtException(Reason.INVALID_SOURCE, name);
        }
        try (InputStream in = Files.newInputStream(file)) {
            return LimitedReader.readAll(in, maxBytes);
        } catch (IOException e) {
            throw new FrameArtException(Reason.FETCH_FAILED, e.getMessage(), e);
        }
    }

    private Path resolve(String name) {
        try {
            Path file = directory.resolve(name).normalize();
            if (!file.startsWith(directory)) {
                throw new FrameArtException(Reason.SOURCE_NOT_ALLOWED, name);
            }
            return file;
        } catch (InvalidPathException e) {
            throw new FrameArtException(Reason.INVALID_SOURCE, name, e);
        }
    }
}
