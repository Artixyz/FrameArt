package dev.frameart.core.application.render;

import dev.frameart.core.domain.FrameArtException;
import dev.frameart.core.domain.MapTile;
import dev.frameart.core.domain.TileGrid;

/** Resolve quantos mapas uma imagem ocupa, respeitando os limites configurados. */
public final class SizingPolicy {

    private final int maxColumns;
    private final int maxRows;

    public SizingPolicy(int maxColumns, int maxRows) {
        this.maxColumns = Math.max(1, maxColumns);
        this.maxRows = Math.max(1, maxRows);
    }

    public void validate(TileGrid grid) {
        if (grid.columns() > maxColumns || grid.rows() > maxRows) {
            throw new FrameArtException(FrameArtException.Reason.GRID_TOO_LARGE, maxColumns + "x" + maxRows);
        }
    }

    public TileGrid resolve(GridRequest request, int imageWidth, int imageHeight) {
        TileGrid grid = compute(request, imageWidth, imageHeight);
        validate(grid);
        return grid;
    }

    private TileGrid compute(GridRequest request, int width, int height) {
        if (request.isExact()) {
            return request.toGrid();
        }
        double aspect = (double) height / width;
        if (request.columns().isPresent()) {
            int columns = request.columns().getAsInt();
            return new TileGrid(columns, Math.max(1, (int) Math.round(columns * aspect)));
        }
        if (request.rows().isPresent()) {
            int rows = request.rows().getAsInt();
            return new TileGrid(Math.max(1, (int) Math.round(rows / aspect)), rows);
        }
        int columns = (int) Math.ceil(width / (double) MapTile.SIZE);
        int rows = (int) Math.ceil(height / (double) MapTile.SIZE);
        if (columns > maxColumns || rows > maxRows) {
            double scale = Math.min(maxColumns / (double) columns, maxRows / (double) rows);
            columns = (int) Math.floor(columns * scale);
            rows = (int) Math.floor(rows * scale);
        }
        return new TileGrid(Math.max(1, columns), Math.max(1, rows));
    }
}
