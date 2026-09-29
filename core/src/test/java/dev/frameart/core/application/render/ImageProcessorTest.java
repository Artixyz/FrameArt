package dev.frameart.core.application.render;

import dev.frameart.core.application.port.ColorPalette;
import dev.frameart.core.domain.MapTile;
import dev.frameart.core.domain.RenderedImage;
import dev.frameart.core.domain.TileGrid;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ImageProcessorTest {

    /** Paleta de teste: 1 = preto, 2 = branco. */
    private static final ColorPalette BLACK_AND_WHITE = new ColorPalette() {
        @Override
        public String id() {
            return "test";
        }

        @Override
        public byte match(int argb) {
            int luminance = ((argb >> 16) & 0xFF) + ((argb >> 8) & 0xFF) + (argb & 0xFF);
            return luminance < 384 ? (byte) 1 : (byte) 2;
        }

        @Override
        public int rgb(byte index) {
            return index == 1 ? 0x000000 : 0xFFFFFF;
        }
    };

    @Test
    void slicesLeftAndRightHalvesIntoSeparateTiles() {
        BufferedImage image = new BufferedImage(256, 128, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, 128, 128);
        graphics.setColor(Color.WHITE);
        graphics.fillRect(128, 0, 128, 128);
        graphics.dispose();

        RenderedImage rendered = new ImageProcessor(BLACK_AND_WHITE).process(image, new TileGrid(2, 1), false);

        assertEquals(1, rendered.tile(0, 0)[64 * MapTile.SIZE + 64]);
        assertEquals(2, rendered.tile(1, 0)[64 * MapTile.SIZE + 64]);
    }

    @Test
    void ditheringMixesColorsForMidTones() {
        int[] gray = new int[16 * 16];
        java.util.Arrays.fill(gray, 0xFF808080);

        byte[] result = new FloydSteinbergQuantizer().quantize(gray, 16, 16, BLACK_AND_WHITE);

        long black = 0;
        for (byte b : result) {
            if (b == 1) {
                black++;
            }
        }
        // ~50% cinza deve virar aproximadamente metade preto, metade branco.
        assertEquals(128, black, 20);
    }

    @Test
    void transparentPixelsStayTransparent() {
        byte[] result = new NearestColorQuantizer().quantize(new int[]{0x00FFFFFF}, 1, 1, BLACK_AND_WHITE);
        assertEquals(MapTile.TRANSPARENT, result[0]);
    }
}
