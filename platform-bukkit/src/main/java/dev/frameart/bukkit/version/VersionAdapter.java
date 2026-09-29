package dev.frameart.bukkit.version;

import org.bukkit.World;
import org.bukkit.entity.ItemFrame;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapView;

import java.util.OptionalInt;

/**
 * Isola tudo que muda entre 1.8 e 26.x (IDs short/int, material do mapa, MapMeta, molduras).
 * Cada implementação é compilada contra a API da sua faixa de versões.
 */
public interface VersionAdapter {

    String name();

    MapView createMap(World world);

    /** Retorna {@code null} se o mapa não existir. */
    MapView findMap(int id);

    int mapId(MapView view);

    /** ID do mapa contido no item, se for um mapa preenchido. */
    OptionalInt mapIdOf(ItemStack item);

    ItemStack createMapItem(MapView view);

    /** Ajustes no MapView (travar, sem cursor de posição...). */
    void prepareMapView(MapView view);

    void decorateFrame(ItemFrame frame, FrameOptions options);
}
