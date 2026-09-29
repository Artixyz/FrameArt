package dev.frameart.adapter.modern;

import dev.frameart.bukkit.version.FrameOptions;
import dev.frameart.bukkit.version.VersionAdapter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.ItemFrame;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;

import java.util.OptionalInt;

/**
 * 1.13 – 26.x: {@code FILLED_MAP} + {@link MapMeta}. Métodos que surgiram depois da 1.13
 * são chamados com fallback via {@link NoSuchMethodError}, então um único adaptador cobre a faixa toda.
 */
@SuppressWarnings("deprecation")
public final class ModernVersionAdapter implements VersionAdapter {

    @Override
    public String name() {
        return "modern (1.13+)";
    }

    @Override
    public MapView createMap(World world) {
        return Bukkit.createMap(world);
    }

    @Override
    public MapView findMap(int id) {
        return Bukkit.getMap(id);
    }

    @Override
    public int mapId(MapView view) {
        return view.getId();
    }

    @Override
    public OptionalInt mapIdOf(ItemStack item) {
        if (item == null || item.getType() != Material.FILLED_MAP) {
            return OptionalInt.empty();
        }
        ItemMeta meta = item.getItemMeta();
        if (!(meta instanceof MapMeta mapMeta)) {
            return OptionalInt.empty();
        }
        try {
            // 1.14+
            MapView view = mapMeta.hasMapView() ? mapMeta.getMapView() : null;
            return view == null ? OptionalInt.empty() : OptionalInt.of(view.getId());
        } catch (NoSuchMethodError legacyMeta) {
            // 1.13.x
            return mapMeta.hasMapId() ? OptionalInt.of(mapMeta.getMapId()) : OptionalInt.empty();
        }
    }

    @Override
    public ItemStack createMapItem(MapView view) {
        ItemStack item = new ItemStack(Material.FILLED_MAP);
        MapMeta meta = (MapMeta) item.getItemMeta();
        try {
            meta.setMapView(view); // 1.14+
        } catch (NoSuchMethodError legacyMeta) {
            meta.setMapId(view.getId()); // 1.13.x
        }
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public void prepareMapView(MapView view) {
        view.setScale(MapView.Scale.CLOSEST);
        attempt(() -> view.setTrackingPosition(false));
        attempt(() -> view.setUnlimitedTracking(false));
        attempt(() -> view.setLocked(true)); // 1.14+
    }

    @Override
    public void decorateFrame(ItemFrame frame, FrameOptions options) {
        attempt(() -> frame.setVisible(!options.invisible())); // 1.16+
        attempt(() -> frame.setFixed(options.fixed()));        // 1.16+
    }

    /** Executa uma chamada que pode não existir na versão atual do servidor. */
    private static void attempt(Runnable call) {
        try {
            call.run();
        } catch (NoSuchMethodError | UnsupportedOperationException unsupported) {
            // Recurso indisponível nesta versão: ignorado de propósito.
        }
    }
}
