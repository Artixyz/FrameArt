package dev.frameart.core.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Região de uma parede ocupada por molduras.
 * {@code origin} é o bloco da moldura no canto inferior esquerdo (visto de frente);
 * a imagem cresce para a direita e para cima.
 */
public record PlacementArea(BlockPosition origin, Facing facing, TileGrid grid) {

    public PlacementArea {
        Objects.requireNonNull(origin, "origin");
        Objects.requireNonNull(facing, "facing");
        Objects.requireNonNull(grid, "grid");
    }

    /** Posição da moldura para a coluna/linha (linha 0 = topo da imagem). */
    public BlockPosition framePosition(int column, int row) {
        return origin.relative(facing.viewerRight(), column).above(grid.rows() - 1 - row);
    }

    /** Posições em ordem row-major (mesma ordem dos tiles). */
    public List<BlockPosition> framePositions() {
        List<BlockPosition> positions = new ArrayList<>(grid.tileCount());
        for (int row = 0; row < grid.rows(); row++) {
            for (int column = 0; column < grid.columns(); column++) {
                positions.add(framePosition(column, row));
            }
        }
        return positions;
    }
}
