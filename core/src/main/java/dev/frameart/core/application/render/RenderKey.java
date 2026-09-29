package dev.frameart.core.application.render;

import dev.frameart.core.domain.TileGrid;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/** Chave de cache: mesma origem + tamanho + dithering + paleta = mesmo resultado. */
public record RenderKey(String source, TileGrid grid, boolean dithering, String paletteId) {

    /** Hash SHA-256 em hexadecimal, seguro para nome de arquivo. */
    public String digest() {
        String raw = source + '\n' + grid + '\n' + dithering + '\n' + paletteId;
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(sha.digest(raw.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}
