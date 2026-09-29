package dev.frameart.core.domain;

/**
 * Direção horizontal para a qual a moldura (item frame) está virada,
 * ou seja, a direção de quem olha a imagem de frente é {@link #opposite()}.
 */
public enum Facing {
    NORTH(0, -1),
    EAST(1, 0),
    SOUTH(0, 1),
    WEST(-1, 0);

    private final int dx;
    private final int dz;

    Facing(int dx, int dz) {
        this.dx = dx;
        this.dz = dz;
    }

    public int dx() {
        return dx;
    }

    public int dz() {
        return dz;
    }

    public Facing opposite() {
        return values()[(ordinal() + 2) % 4];
    }

    public Facing clockwise() {
        return values()[(ordinal() + 1) % 4];
    }

    /** Direção "direita" do ponto de vista de quem olha a moldura de frente. */
    public Facing viewerRight() {
        return opposite().clockwise();
    }
}
