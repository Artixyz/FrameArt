package dev.frameart.core.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlacementAreaTest {

    @Test
    void viewerRightIsClockwiseOfViewDirection() {
        // Moldura virada para o sul => jogador olha para o norte => direita = leste.
        assertEquals(Facing.EAST, Facing.SOUTH.viewerRight());
        assertEquals(Facing.WEST, Facing.NORTH.viewerRight());
        assertEquals(Facing.NORTH, Facing.EAST.viewerRight());
        assertEquals(Facing.SOUTH, Facing.WEST.viewerRight());
    }

    @Test
    void topLeftTileIsAboveOriginAndGridGrowsToTheRight() {
        PlacementArea area = new PlacementArea(new BlockPosition("world", 0, 64, 0), Facing.SOUTH, new TileGrid(3, 2));

        assertEquals(new BlockPosition("world", 0, 65, 0), area.framePosition(0, 0));
        assertEquals(new BlockPosition("world", 2, 65, 0), area.framePosition(2, 0));
        assertEquals(new BlockPosition("world", 0, 64, 0), area.framePosition(0, 1));
        assertEquals(6, area.framePositions().size());
    }
}
