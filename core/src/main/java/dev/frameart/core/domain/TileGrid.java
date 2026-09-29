package dev.frameart.core.domain;

/** Quantidade de mapas (colunas x linhas) que uma imagem ocupa. */
public record TileGrid(int columns, int rows) {

    public TileGrid {
        if (columns < 1 || rows < 1) {
            throw new IllegalArgumentException("Grid must be at least 1x1, got " + columns + "x" + rows);
        }
    }

    public int tileCount() {
        return columns * rows;
    }

    public int pixelWidth() {
        return columns * MapTile.SIZE;
    }

    public int pixelHeight() {
        return rows * MapTile.SIZE;
    }

    @Override
    public String toString() {
        return columns + "x" + rows;
    }
}
