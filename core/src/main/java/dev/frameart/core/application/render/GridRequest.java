package dev.frameart.core.application.render;

import dev.frameart.core.domain.TileGrid;

import java.util.OptionalInt;

/** Tamanho pedido pelo usuário. Dimensões ausentes são calculadas pela proporção da imagem. */
public record GridRequest(OptionalInt columns, OptionalInt rows) {

    public static GridRequest auto() {
        return new GridRequest(OptionalInt.empty(), OptionalInt.empty());
    }

    public static GridRequest exact(TileGrid grid) {
        return new GridRequest(OptionalInt.of(grid.columns()), OptionalInt.of(grid.rows()));
    }

    public boolean isExact() {
        return columns.isPresent() && rows.isPresent();
    }

    public TileGrid toGrid() {
        return new TileGrid(columns.orElseThrow(), rows.orElseThrow());
    }
}
