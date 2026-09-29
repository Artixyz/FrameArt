package dev.frameart.bukkit.world;

import dev.frameart.bukkit.render.ImageTileRenderer;
import dev.frameart.bukkit.version.FrameOptions;
import dev.frameart.bukkit.version.VersionAdapter;
import dev.frameart.core.application.port.WorldDisplay;
import dev.frameart.core.domain.BlockPosition;
import dev.frameart.core.domain.FrameArtException;
import dev.frameart.core.domain.FrameArtException.Reason;
import dev.frameart.core.domain.ImagePlacement;
import dev.frameart.core.domain.PlacementArea;
import dev.frameart.core.domain.RenderedImage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.ItemFrame;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.OptionalInt;
import java.util.Set;
import java.util.logging.Logger;

/** Implementação da porta de mundo usando entidades ItemFrame + MapView. */
public final class BukkitWorldDisplay implements WorldDisplay {

    private static final double SEARCH_RADIUS = 0.6;

    private final VersionAdapter adapter;
    private final FrameOptions frameOptions;
    private final Logger logger;

    public BukkitWorldDisplay(VersionAdapter adapter, FrameOptions frameOptions, Logger logger) {
        this.adapter = adapter;
        this.frameOptions = frameOptions;
        this.logger = logger;
    }

    @Override
    public void ensurePlaceable(PlacementArea area) {
        World world = requireWorld(area.origin().world());
        BlockFace wallDirection = BukkitConversions.toBlockFace(area.facing()).getOppositeFace();
        for (BlockPosition position : area.framePositions()) {
            Block block = blockAt(world, position);
            if (!block.isEmpty() || hasHangingEntity(block)) {
                throw new FrameArtException(Reason.AREA_OBSTRUCTED, position.toString());
            }
            if (!block.getRelative(wallDirection).getType().isSolid()) {
                throw new FrameArtException(Reason.MISSING_WALL, position.toString());
            }
        }
    }

    @Override
    public List<Integer> place(PlacementArea area, RenderedImage image) {
        ensurePlaceable(area);
        World world = requireWorld(area.origin().world());
        BlockFace face = BukkitConversions.toBlockFace(area.facing());
        List<ItemFrame> spawned = new ArrayList<>();
        List<Integer> mapIds = new ArrayList<>(image.grid().tileCount());
        try {
            for (int row = 0; row < image.grid().rows(); row++) {
                for (int column = 0; column < image.grid().columns(); column++) {
                    MapView view = adapter.createMap(world);
                    install(view, image.tile(column, row));
                    ItemFrame frame = spawnFrame(world, area.framePosition(column, row), face);
                    spawned.add(frame);
                    frame.setItem(adapter.createMapItem(view));
                    adapter.decorateFrame(frame, frameOptions);
                    mapIds.add(adapter.mapId(view));
                }
            }
            return mapIds;
        } catch (RuntimeException e) {
            spawned.forEach(Entity::remove);
            if (e instanceof FrameArtException frameArt) {
                throw frameArt;
            }
            throw new FrameArtException(Reason.PLACEMENT_FAILED, e.getMessage(), e);
        }
    }

    @Override
    public int attach(ImagePlacement placement, RenderedImage image) {
        int attached = 0;
        for (int row = 0; row < placement.grid().rows(); row++) {
            for (int column = 0; column < placement.grid().columns(); column++) {
                int mapId = placement.mapIdAt(column, row);
                MapView view = adapter.findMap(mapId);
                if (view == null) {
                    logger.warning("Map #" + mapId + " of image '" + placement.id() + "' no longer exists.");
                    continue;
                }
                install(view, image.tile(column, row));
                attached++;
            }
        }
        return attached;
    }

    @Override
    public void remove(ImagePlacement placement) {
        Set<Integer> mapIds = new HashSet<>(placement.mapIds());
        World world = Bukkit.getWorld(placement.area().origin().world());
        if (world != null) {
            for (BlockPosition position : placement.area().framePositions()) {
                removeFramesAt(blockAt(world, position), mapIds);
            }
        }
        for (int mapId : mapIds) {
            MapView view = adapter.findMap(mapId);
            if (view != null) {
                for (MapRenderer renderer : new ArrayList<>(view.getRenderers())) {
                    if (renderer instanceof ImageTileRenderer) {
                        view.removeRenderer(renderer);
                    }
                }
            }
        }
    }

    private void install(MapView view, byte[] tile) {
        for (MapRenderer renderer : new ArrayList<>(view.getRenderers())) {
            view.removeRenderer(renderer);
        }
        view.addRenderer(new ImageTileRenderer(tile));
        adapter.prepareMapView(view);
    }

    private ItemFrame spawnFrame(World world, BlockPosition position, BlockFace face) {
        Location location = new Location(world, position.x(), position.y(), position.z());
        ItemFrame frame = world.spawn(location, ItemFrame.class);
        if (!frame.setFacingDirection(face, true) || frame.getFacing() != face) {
            frame.remove();
            throw new FrameArtException(Reason.PLACEMENT_FAILED, position.toString());
        }
        return frame;
    }

    private void removeFramesAt(Block block, Set<Integer> mapIds) {
        for (Entity entity : nearby(block)) {
            if (entity instanceof ItemFrame frame) {
                OptionalInt id = adapter.mapIdOf(frame.getItem());
                if (id.isPresent() && mapIds.contains(id.getAsInt())) {
                    frame.remove();
                }
            }
        }
    }

    private boolean hasHangingEntity(Block block) {
        for (Entity entity : nearby(block)) {
            if (entity instanceof Hanging && entity.getLocation().getBlock().equals(block)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Usa {@code Chunk#getEntities()} (Entity[] em todas as versões) em vez de
     * {@code World#getNearbyEntities}, cujo tipo de retorno mudou de List para Collection.
     */
    private static List<Entity> nearby(Block block) {
        double cx = block.getX() + 0.5;
        double cy = block.getY() + 0.5;
        double cz = block.getZ() + 0.5;
        List<Entity> result = new ArrayList<>();
        for (Entity entity : block.getChunk().getEntities()) {
            Location location = entity.getLocation();
            if (Math.abs(location.getX() - cx) <= SEARCH_RADIUS
                    && Math.abs(location.getY() - cy) <= SEARCH_RADIUS
                    && Math.abs(location.getZ() - cz) <= SEARCH_RADIUS) {
                result.add(entity);
            }
        }
        return result;
    }

    private static Block blockAt(World world, BlockPosition position) {
        return world.getBlockAt(position.x(), position.y(), position.z());
    }

    private static World requireWorld(String name) {
        World world = Bukkit.getWorld(name);
        if (world == null) {
            throw new FrameArtException(Reason.WORLD_NOT_LOADED, name);
        }
        return world;
    }
}
