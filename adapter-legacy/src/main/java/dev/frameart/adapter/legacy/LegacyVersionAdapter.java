package dev.frameart.adapter.legacy;

import dev.frameart.bukkit.version.FrameOptions;
import dev.frameart.bukkit.version.VersionAdapter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.ItemFrame;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapView;

import java.util.OptionalInt;

/**
 * 1.8 – 1.12.2: {@code MapView#getId()} retorna short e o mapa é
 * {@code Material.MAP} com o ID na durabilidade. Molduras invisíveis/fixas não existem.
 */
@SuppressWarnings("deprecation")
public final class LegacyVersionAdapter implements VersionAdapter {

    @Override
    public String name() {
        return "legacy (1.8 - 1.12.2)";
    }

    @Override
    public MapView createMap(World world) {
        return Bukkit.createMap(world);
    }

    @Override
    public MapView findMap(int id) {
        return Bukkit.getMap((short) id);
    }

    @Override
    public int mapId(MapView view) {
        return view.getId() & 0xFFFF;
    }

    @Override
    public OptionalInt mapIdOf(ItemStack item) {
        if (item == null || item.getType() != Material.MAP) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(item.getDurability() & 0xFFFF);
    }

    @Override
    public ItemStack createMapItem(MapView view) {
        return new ItemStack(Material.MAP, 1, view.getId());
    }

    @Override
    public void prepareMapView(MapView view) {
        // Nada a ajustar nessas versões.
    }

    @Override
    public void decorateFrame(ItemFrame frame, FrameOptions options) {
        // Molduras invisíveis/fixas só existem a partir da 1.16.
    }
}
