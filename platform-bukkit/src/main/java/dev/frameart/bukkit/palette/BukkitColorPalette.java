package dev.frameart.bukkit.palette;

import dev.frameart.core.application.port.ColorPalette;
import dev.frameart.core.domain.MapTile;
import org.bukkit.map.MapPalette;

import java.awt.Color;
import java.util.Arrays;

/**
 * Usa a paleta do próprio servidor (que muda entre versões) com uma tabela de lookup
 * preenchida sob demanda: 6 bits por canal = 262.144 entradas, ~512 KB.
 * Depois de aquecida, cada pixel custa um acesso a array em vez de ~250 comparações.
 */
@SuppressWarnings("deprecation")
public final class BukkitColorPalette implements ColorPalette {

    private static final int BITS = 6;
    private static final int SHIFT = 8 - BITS;
    private static final int HALF_STEP = 1 << (SHIFT - 1);
    private static final short UNKNOWN = -1;

    private final String id;
    private final short[] lookup = new short[1 << (BITS * 3)];
    private final int[] rgbByIndex = new int[256];

    public BukkitColorPalette(String serverVersion) {
        this.id = "bukkit-" + serverVersion;
        Arrays.fill(lookup, UNKNOWN);
        for (int i = 0; i < rgbByIndex.length; i++) {
            try {
                rgbByIndex[i] = MapPalette.getColor((byte) i).getRGB();
            } catch (RuntimeException outOfPalette) {
                rgbByIndex[i] = 0;
            }
        }
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public byte match(int argb) {
        if ((argb >>> 24) < 128) {
            return MapTile.TRANSPARENT;
        }
        int r = ((argb >> 16) & 0xFF) >> SHIFT;
        int g = ((argb >> 8) & 0xFF) >> SHIFT;
        int b = (argb & 0xFF) >> SHIFT;
        int key = (r << (BITS * 2)) | (g << BITS) | b;

        short cached = lookup[key];
        if (cached != UNKNOWN) {
            return (byte) cached;
        }
        // Corrida benigna: dois threads calculariam o mesmo valor.
        byte matched = MapPalette.matchColor(new Color(center(r), center(g), center(b)));
        lookup[key] = (short) (matched & 0xFF);
        return matched;
    }

    @Override
    public int rgb(byte index) {
        return rgbByIndex[index & 0xFF];
    }

    private static int center(int bucket) {
        return (bucket << SHIFT) | HALF_STEP;
    }
}
