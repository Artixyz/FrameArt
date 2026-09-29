package dev.frameart.bukkit.render;

import dev.frameart.core.domain.MapTile;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapCursorCollection;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

/**
 * Desenha um tile pré-processado. Não contextual: o canvas é compartilhado entre
 * jogadores, então desenhamos uma única vez e o servidor só reenvia o buffer.
 */
public final class ImageTileRenderer extends MapRenderer {

    private final byte[] pixels;
    private volatile boolean drawn;

    public ImageTileRenderer(byte[] pixels) {
        super(false);
        this.pixels = pixels;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void render(MapView view, MapCanvas canvas, Player player) {
        if (drawn) {
            return;
        }
        MapCursorCollection cursors = canvas.getCursors();
        while (cursors.size() > 0) {
            cursors.removeCursor(cursors.getCursor(0));
        }
        for (int y = 0; y < MapTile.SIZE; y++) {
            int row = y * MapTile.SIZE;
            for (int x = 0; x < MapTile.SIZE; x++) {
                canvas.setPixel(x, y, pixels[row + x]);
            }
        }
        drawn = true;
    }
}
