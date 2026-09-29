package dev.frameart.infrastructure.cache;

import dev.frameart.core.application.port.RenderCache;
import dev.frameart.core.application.render.RenderKey;
import dev.frameart.core.domain.MapTile;
import dev.frameart.core.domain.RenderedImage;
import dev.frameart.core.domain.TileGrid;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Cache persistente: cada imagem processada vira um arquivo {@code <sha256>.fart} (GZIP).
 * Permite restaurar todas as imagens no boot sem baixá-las novamente.
 */
public final class DiskRenderCache implements RenderCache {

    private static final int MAGIC = 0x46415254; // "FART"
    private static final int FORMAT_VERSION = 1;
    private static final String EXTENSION = ".fart";

    private final Path directory;
    private final Logger logger;

    public DiskRenderCache(Path directory, Logger logger) {
        this.directory = directory;
        this.logger = logger;
    }

    @Override
    public Optional<RenderedImage> get(RenderKey key) {
        Path file = fileOf(key);
        if (!Files.isRegularFile(file)) {
            return Optional.empty();
        }
        try (DataInputStream in = new DataInputStream(
                new GZIPInputStream(new BufferedInputStream(Files.newInputStream(file))))) {
            if (in.readInt() != MAGIC || in.readInt() != FORMAT_VERSION) {
                Files.deleteIfExists(file);
                return Optional.empty();
            }
            TileGrid grid = new TileGrid(in.readInt(), in.readInt());
            if (!grid.equals(key.grid())) {
                return Optional.empty();
            }
            List<byte[]> tiles = new ArrayList<>(grid.tileCount());
            for (int i = 0; i < grid.tileCount(); i++) {
                byte[] tile = new byte[MapTile.PIXELS];
                in.readFully(tile);
                tiles.add(tile);
            }
            return Optional.of(new RenderedImage(grid, tiles));
        } catch (IOException | RuntimeException e) {
            logger.log(Level.WARNING, "Discarding corrupted cache file " + file.getFileName(), e);
            deleteQuietly(file);
            return Optional.empty();
        }
    }

    @Override
    public void put(RenderKey key, RenderedImage image) {
        Path target = fileOf(key);
        try {
            Files.createDirectories(directory);
            Path temp = Files.createTempFile(directory, "tmp-", EXTENSION);
            try (DataOutputStream out = new DataOutputStream(
                    new GZIPOutputStream(new BufferedOutputStream(Files.newOutputStream(temp))))) {
                out.writeInt(MAGIC);
                out.writeInt(FORMAT_VERSION);
                out.writeInt(image.grid().columns());
                out.writeInt(image.grid().rows());
                for (byte[] tile : image.tiles()) {
                    out.write(tile);
                }
            }
            Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            logger.log(Level.WARNING, "Could not write cache file " + target.getFileName(), e);
        }
    }

    @Override
    public void clear() {
        if (!Files.isDirectory(directory)) {
            return;
        }
        try (DirectoryStream<Path> files = Files.newDirectoryStream(directory, "*" + EXTENSION)) {
            for (Path file : files) {
                deleteQuietly(file);
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, "Could not clear render cache", e);
        }
    }

    private Path fileOf(RenderKey key) {
        return directory.resolve(key.digest() + EXTENSION);
    }

    private static void deleteQuietly(Path file) {
        try {
            Files.deleteIfExists(file);
        } catch (IOException ignored) {
            // melhor esforço
        }
    }
}
