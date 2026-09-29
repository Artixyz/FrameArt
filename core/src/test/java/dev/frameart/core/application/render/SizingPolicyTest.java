package dev.frameart.core.application.render;

import dev.frameart.core.domain.FrameArtException;
import dev.frameart.core.domain.TileGrid;
import org.junit.jupiter.api.Test;

import java.util.OptionalInt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SizingPolicyTest {

    private final SizingPolicy policy = new SizingPolicy(10, 10);

    @Test
    void autoUsesNativeSize() {
        assertEquals(new TileGrid(2, 1), policy.resolve(GridRequest.auto(), 256, 128));
    }

    @Test
    void autoScalesDownToLimits() {
        assertEquals(new TileGrid(10, 5), policy.resolve(GridRequest.auto(), 4096, 2048));
    }

    @Test
    void missingDimensionKeepsAspectRatio() {
        GridRequest onlyColumns = new GridRequest(OptionalInt.of(4), OptionalInt.empty());
        assertEquals(new TileGrid(4, 2), policy.resolve(onlyColumns, 1920, 1080));
    }

    @Test
    void rejectsGridAboveLimit() {
        GridRequest tooBig = new GridRequest(OptionalInt.of(20), OptionalInt.of(1));
        FrameArtException error = assertThrows(FrameArtException.class, () -> policy.resolve(tooBig, 10, 10));
        assertEquals(FrameArtException.Reason.GRID_TOO_LARGE, error.reason());
    }
}
