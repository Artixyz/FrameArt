package dev.frameart.bukkit.persistence;

import dev.frameart.core.application.port.ManagedMapLookup;
import dev.frameart.core.application.port.PlacementRepository;
import dev.frameart.core.domain.BlockPosition;
import dev.frameart.core.domain.Facing;
import dev.frameart.core.domain.ImageId;
import dev.frameart.core.domain.ImagePlacement;
import dev.frameart.core.domain.PlacementArea;
import dev.frameart.core.domain.TileGrid;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Todas as localizações ficam em {@code locations.yml}. Mantém um espelho em memória
 * (leituras O(1), inclusive o índice de IDs de mapa usado pela proteção de molduras).
 */
public final class YamlPlacementRepository implements PlacementRepository, ManagedMapLookup {

    private static final String ROOT = "images";

    private final File file;
    private final Logger logger;
    private final Map<ImageId, ImagePlacement> placements = new ConcurrentHashMap<>();
    private final Set<Integer> managedMaps = ConcurrentHashMap.newKeySet();

    public YamlPlacementRepository(File file, Logger logger) {
        this.file = file;
        this.logger = logger;
        reload();
    }

    @Override
    public Optional<ImagePlacement> find(ImageId id) {
        return Optional.ofNullable(placements.get(id));
    }

    @Override
    public Collection<ImagePlacement> findAll() {
        return List.copyOf(placements.values());
    }

    @Override
    public synchronized void save(ImagePlacement placement) {
        ImagePlacement previous = placements.put(placement.id(), placement);
        if (previous != null) {
            previous.mapIds().forEach(managedMaps::remove);
        }
        managedMaps.addAll(placement.mapIds());
        persist();
    }

    @Override
    public synchronized void delete(ImageId id) {
        ImagePlacement removed = placements.remove(id);
        if (removed != null) {
            removed.mapIds().forEach(managedMaps::remove);
            persist();
        }
    }

    @Override
    public synchronized void reload() {
        placements.clear();
        managedMaps.clear();
        if (!file.exists()) {
            return;
        }
        ConfigurationSection root = YamlConfiguration.loadConfiguration(file).getConfigurationSection(ROOT);
        if (root == null) {
            return;
        }
        for (String key : root.getKeys(false)) {
            try {
                ImagePlacement placement = read(key, root.getConfigurationSection(key));
                placements.put(placement.id(), placement);
                managedMaps.addAll(placement.mapIds());
            } catch (RuntimeException e) {
                logger.log(Level.WARNING, "Invalid entry '" + key + "' in " + file.getName() + ": " + e.getMessage());
            }
        }
    }

    @Override
    public boolean isManaged(int mapId) {
        return managedMaps.contains(mapId);
    }

    private static ImagePlacement read(String key, ConfigurationSection section) {
        if (section == null) {
            throw new IllegalArgumentException("not a section");
        }
        ConfigurationSection location = require(section.getConfigurationSection("location"), "location");
        ConfigurationSection size = require(section.getConfigurationSection("size"), "size");
        BlockPosition origin = new BlockPosition(
                require(location.getString("world"), "location.world"),
                location.getInt("x"), location.getInt("y"), location.getInt("z"));
        Facing facing = Facing.valueOf(require(location.getString("facing"), "location.facing").toUpperCase(Locale.ROOT));
        TileGrid grid = new TileGrid(size.getInt("columns"), size.getInt("rows"));
        return new ImagePlacement(
                ImageId.of(key),
                require(section.getString("source"), "source"),
                new PlacementArea(origin, facing, grid),
                section.getBoolean("dithering", true),
                section.getIntegerList("maps"),
                Instant.ofEpochMilli(section.getLong("created-at", 0L)));
    }

    private void persist() {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.createSection(ROOT);
        placements.values().stream()
                .sorted(Comparator.comparing(placement -> placement.id().value()))
                .forEach(placement -> write(yaml, ROOT + "." + placement.id().value(), placement));
        try {
            yaml.save(file);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Could not save " + file.getName(), e);
        }
    }

    private static void write(YamlConfiguration yaml, String path, ImagePlacement placement) {
        BlockPosition origin = placement.area().origin();
        yaml.set(path + ".source", placement.source());
        yaml.set(path + ".location.world", origin.world());
        yaml.set(path + ".location.x", origin.x());
        yaml.set(path + ".location.y", origin.y());
        yaml.set(path + ".location.z", origin.z());
        yaml.set(path + ".location.facing", placement.area().facing().name());
        yaml.set(path + ".size.columns", placement.grid().columns());
        yaml.set(path + ".size.rows", placement.grid().rows());
        yaml.set(path + ".dithering", placement.dithering());
        yaml.set(path + ".maps", placement.mapIds());
        yaml.set(path + ".created-at", placement.createdAt().toEpochMilli());
    }

    private static <T> T require(T value, String field) {
        if (value == null) {
            throw new IllegalArgumentException("missing '" + field + "'");
        }
        return value;
    }
}
