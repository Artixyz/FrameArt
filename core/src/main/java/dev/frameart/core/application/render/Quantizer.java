package dev.frameart.core.application.render;

import dev.frameart.core.application.port.ColorPalette;

/** Estratégia de conversão de pixels ARGB para índices da paleta de mapas. */
public interface Quantizer {

    byte[] quantize(int[] argb, int width, int height, ColorPalette palette);

    static Quantizer of(boolean dithering) {
        return dithering ? new FloydSteinbergQuantizer() : new NearestColorQuantizer();
    }

    static boolean isTransparent(int argb) {
        return (argb >>> 24) < 128;
    }
}
