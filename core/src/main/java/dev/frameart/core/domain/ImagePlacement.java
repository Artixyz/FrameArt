package dev.frameart.core.domain;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/** Uma imagem fixada no mundo: origem, área, e os IDs de mapa de cada tile (row-major). */
public record ImagePlacement(
        ImageId id,
        String source,
        PlacementArea area,
        boolean dithering,
        List<Integer> mapIds,
        Instant createdAt) {

    public ImagePlacement {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(area, "area");
        Objects.requireNonNull(createdAt, "createdAt");
        mapIds = List.copyOf(mapIds);
        if (mapIds.size() != area.grid().tileCount()) {
            throw new IllegalArgumentException("Expected " + area.grid().tileCount() + " map ids, got " + mapIds.size());
        }
    }

    public TileGrid grid() {
        return area.grid();
    }

    public int mapIdAt(int column, int row) {
        return mapIds.get(row * area.grid().columns() + column);
    }
}
