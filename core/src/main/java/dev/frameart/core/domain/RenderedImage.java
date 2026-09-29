package dev.frameart.core.domain;

import java.util.List;
import java.util.Objects;

/** Imagem já convertida para a paleta de mapas e fatiada em tiles de 128x128 (row-major). */
public record RenderedImage(TileGrid grid, List<byte[]> tiles) {

    public RenderedImage {
        Objects.requireNonNull(grid, "grid");
        tiles = List.copyOf(tiles);
        if (tiles.size() != grid.tileCount()) {
            throw new IllegalArgumentException("Expected " + grid.tileCount() + " tiles, got " + tiles.size());
        }
        for (byte[] tile : tiles) {
            if (tile.length != MapTile.PIXELS) {
                throw new IllegalArgumentException("Tile must have " + MapTile.PIXELS + " pixels");
            }
        }
    }

    public byte[] tile(int column, int row) {
        return tiles.get(row * grid.columns() + column);
    }
}
