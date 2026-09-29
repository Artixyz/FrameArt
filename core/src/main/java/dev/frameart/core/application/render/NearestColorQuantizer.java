package dev.frameart.core.application.render;

import dev.frameart.core.application.port.ColorPalette;
import dev.frameart.core.domain.MapTile;

/** Cor mais próxima, sem difusão de erro. Mais rápido, ideal para pixel art. */
public final class NearestColorQuantizer implements Quantizer {

    @Override
    public byte[] quantize(int[] argb, int width, int height, ColorPalette palette) {
        byte[] out = new byte[width * height];
        for (int i = 0; i < out.length; i++) {
            int pixel = argb[i];
            out[i] = Quantizer.isTransparent(pixel) ? MapTile.TRANSPARENT : palette.match(pixel);
        }
        return out;
    }
}
