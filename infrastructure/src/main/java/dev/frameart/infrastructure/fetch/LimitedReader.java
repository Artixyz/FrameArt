package dev.frameart.infrastructure.fetch;

import dev.frameart.core.domain.FrameArtException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/** Lê um stream abortando se ultrapassar o limite (protege contra downloads gigantes). */
final class LimitedReader {

    private LimitedReader() {
    }

    static byte[] readAll(InputStream in, long maxBytes) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream(64 * 1024);
        byte[] buffer = new byte[16 * 1024];
        long total = 0;
        int read;
        while ((read = in.read(buffer)) != -1) {
            total += read;
            if (total > maxBytes) {
                throw new FrameArtException(FrameArtException.Reason.IMAGE_TOO_LARGE, (maxBytes / 1024 / 1024) + " MB");
            }
            out.write(buffer, 0, read);
        }
        return out.toByteArray();
    }
}
