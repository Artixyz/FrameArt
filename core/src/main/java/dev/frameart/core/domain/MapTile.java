package dev.frameart.core.domain;

/** Constantes de um mapa do Minecraft. */
public final class MapTile {

    public static final int SIZE = 128;
    public static final int PIXELS = SIZE * SIZE;
    /** Índice de cor transparente na paleta de mapas. */
    public static final byte TRANSPARENT = 0;

    private MapTile() {
    }
}
