package dev.frameart.core.application.render;

import dev.frameart.core.application.port.ColorPalette;
import dev.frameart.core.domain.MapTile;
import dev.frameart.core.domain.RenderedImage;
import dev.frameart.core.domain.TileGrid;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/** Pipeline: redimensionar → quantizar na paleta → fatiar em tiles 128x128. */
public final class ImageProcessor {

    private final ColorPalette palette;

    public ImageProcessor(ColorPalette palette) {
        this.palette = palette;
    }

    public String paletteId() {
        return palette.id();
    }

    public RenderedImage process(BufferedImage source, TileGrid grid, boolean dithering) {
        int width = grid.pixelWidth();
        int height = grid.pixelHeight();
        int[] argb = ImageScaler.fit(source, width, height);
        byte[] indexed = Quantizer.of(dithering).quantize(argb, width, height, palette);
        return new RenderedImage(grid, slice(indexed, grid));
    }

    static List<byte[]> slice(byte[] pixels, TileGrid grid) {
        int width = grid.pixelWidth();
        List<byte[]> tiles = new ArrayList<>(grid.tileCount());
        for (int row = 0; row < grid.rows(); row++) {
            for (int column = 0; column < grid.columns(); column++) {
                byte[] tile = new byte[MapTile.PIXELS];
                for (int y = 0; y < MapTile.SIZE; y++) {
                    int from = (row * MapTile.SIZE + y) * width + column * MapTile.SIZE;
                    System.arraycopy(pixels, from, tile, y * MapTile.SIZE, MapTile.SIZE);
                }
                tiles.add(tile);
            }
        }
        return tiles;
    }
}
