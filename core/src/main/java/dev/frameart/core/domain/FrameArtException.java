package dev.frameart.core.domain;

import java.util.Locale;

/** Erro de negócio. A camada de apresentação traduz {@link Reason} em mensagens. */
public class FrameArtException extends RuntimeException {

    public enum Reason {
        INVALID_NAME,
        NAME_TAKEN,
        NOT_FOUND,
        INVALID_SOURCE,
        SOURCE_NOT_ALLOWED,
        FETCH_FAILED,
        INVALID_IMAGE,
        IMAGE_TOO_LARGE,
        GRID_TOO_LARGE,
        WORLD_NOT_LOADED,
        AREA_OBSTRUCTED,
        MISSING_WALL,
        PLACEMENT_FAILED;

        /** Chave usada no arquivo de mensagens, ex.: {@code name-taken}. */
        public String key() {
            return name().toLowerCase(Locale.ROOT).replace('_', '-');
        }
    }

    private final Reason reason;
    private final String detail;

    public FrameArtException(Reason reason, String detail) {
        this(reason, detail, null);
    }

    public FrameArtException(Reason reason, String detail, Throwable cause) {
        super(reason + (detail == null ? "" : ": " + detail), cause);
        this.reason = reason;
        this.detail = detail == null ? "" : detail;
    }

    public Reason reason() {
        return reason;
    }

    public String detail() {
        return detail;
    }
}
