package dev.frameart.core.domain;

import java.util.Objects;

/** Posição de bloco independente de plataforma. */
public record BlockPosition(String world, int x, int y, int z) {

    public BlockPosition {
        Objects.requireNonNull(world, "world");
    }

    public BlockPosition relative(Facing direction, int distance) {
        return new BlockPosition(world, x + direction.dx() * distance, y, z + direction.dz() * distance);
    }

    public BlockPosition above(int distance) {
        return new BlockPosition(world, x, y + distance, z);
    }

    @Override
    public String toString() {
        return world + " (" + x + ", " + y + ", " + z + ")";
    }
}
