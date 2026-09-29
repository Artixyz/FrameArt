package dev.frameart.core.domain;

import java.util.Locale;
import java.util.regex.Pattern;

/** Identificador único (nome) de uma imagem colocada no mundo. */
public record ImageId(String value) {

    private static final Pattern VALID = Pattern.compile("[a-z0-9_-]{1,32}");

    public ImageId {
        value = value == null ? "" : value.toLowerCase(Locale.ROOT);
        if (!VALID.matcher(value).matches()) {
            throw new FrameArtException(FrameArtException.Reason.INVALID_NAME, value);
        }
    }

    public static ImageId of(String raw) {
        return new ImageId(raw);
    }

    @Override
    public String toString() {
        return value;
    }
}
