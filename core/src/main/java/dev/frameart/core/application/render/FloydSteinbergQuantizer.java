package dev.frameart.core.application.render;

import dev.frameart.core.application.port.ColorPalette;
import dev.frameart.core.domain.MapTile;

import java.util.Arrays;

/**
 * Dithering Floyd–Steinberg. Usa apenas dois buffers de linha para o erro,
 * então o consumo de memória é O(largura) e não O(largura x altura).
 */
public final class FloydSteinbergQuantizer implements Quantizer {

    private static final int CHANNELS = 3;

    @Override
    public byte[] quantize(int[] argb, int width, int height, ColorPalette palette) {
        byte[] out = new byte[width * height];
        // +2 colunas de margem para não precisar checar bordas.
        int[] current = new int[(width + 2) * CHANNELS];
        int[] next = new int[(width + 2) * CHANNELS];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int index = y * width + x;
                int pixel = argb[index];
                if (Quantizer.isTransparent(pixel)) {
                    out[index] = MapTile.TRANSPARENT;
                    continue;
                }
                int offset = (x + 1) * CHANNELS;
                int r = clamp(((pixel >> 16) & 0xFF) + current[offset] / 16);
                int g = clamp(((pixel >> 8) & 0xFF) + current[offset + 1] / 16);
                int b = clamp((pixel & 0xFF) + current[offset + 2] / 16);

                byte color = palette.match(0xFF000000 | (r << 16) | (g << 8) | b);
                out[index] = color;

                int matched = palette.rgb(color);
                diffuse(current, next, offset, 0, r - ((matched >> 16) & 0xFF));
                diffuse(current, next, offset, 1, g - ((matched >> 8) & 0xFF));
                diffuse(current, next, offset, 2, b - (matched & 0xFF));
            }
            int[] swap = current;
            current = next;
            next = swap;
            Arrays.fill(next, 0);
        }
        return out;
    }

    private static void diffuse(int[] current, int[] next, int offset, int channel, int error) {
        int i = offset + channel;
        current[i + CHANNELS] += error * 7;
        next[i - CHANNELS] += error * 3;
        next[i] += error * 5;
        next[i + CHANNELS] += error;
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }
}
