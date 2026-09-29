package dev.frameart.infrastructure.cache;

import dev.frameart.core.application.render.RenderKey;
import dev.frameart.core.domain.MapTile;
import dev.frameart.core.domain.RenderedImage;
import dev.frameart.core.domain.TileGrid;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RenderCacheTest {

    private static final TileGrid GRID = new TileGrid(1, 1);

    private static RenderKey key(String source) {
        return new RenderKey(source, GRID, true, "test");
    }

    private static RenderedImage image(byte fill) {
        byte[] tile = new byte[MapTile.PIXELS];
        java.util.Arrays.fill(tile, fill);
        return new RenderedImage(GRID, List.of(tile));
    }

    @Test
    void memoryCacheEvictsLeastRecentlyUsed() {
        MemoryRenderCache cache = new MemoryRenderCache(2);
        cache.put(key("a"), image((byte) 1));
        cache.put(key("b"), image((byte) 2));
        cache.get(key("a"));
        cache.put(key("c"), image((byte) 3));

        assertTrue(cache.get(key("a")).isPresent());
        assertTrue(cache.get(key("b")).isEmpty());
        assertEquals(2, cache.size());
    }

    @Test
    void diskCacheRoundTrips(@TempDir Path directory) {
        DiskRenderCache cache = new DiskRenderCache(directory, Logger.getAnonymousLogger());
        cache.put(key("x"), image((byte) 42));

        RenderedImage loaded = cache.get(key("x")).orElseThrow();
        assertArrayEquals(image((byte) 42).tile(0, 0), loaded.tile(0, 0));

        cache.clear();
        assertTrue(cache.get(key("x")).isEmpty());
    }

    @Test
    void tieredCachePromotesDiskHitsToMemory(@TempDir Path directory) {
        MemoryRenderCache memory = new MemoryRenderCache(4);
        DiskRenderCache disk = new DiskRenderCache(directory, Logger.getAnonymousLogger());
        disk.put(key("y"), image((byte) 7));

        new TieredRenderCache(memory, disk).get(key("y"));

        assertTrue(memory.get(key("y")).isPresent());
    }
}
