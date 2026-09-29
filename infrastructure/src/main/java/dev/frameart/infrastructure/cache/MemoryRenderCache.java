package dev.frameart.infrastructure.cache;

import dev.frameart.core.application.port.RenderCache;
import dev.frameart.core.application.render.RenderKey;
import dev.frameart.core.domain.RenderedImage;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/** Cache LRU em memória, limitado por quantidade de entradas. Thread-safe. */
public final class MemoryRenderCache implements RenderCache {

    private final Map<RenderKey, RenderedImage> entries;

    public MemoryRenderCache(int maxEntries) {
        int capacity = Math.max(1, maxEntries);
        this.entries = new LinkedHashMap<>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<RenderKey, RenderedImage> eldest) {
                return size() > capacity;
            }
        };
    }

    @Override
    public synchronized Optional<RenderedImage> get(RenderKey key) {
        return Optional.ofNullable(entries.get(key));
    }

    @Override
    public synchronized void put(RenderKey key, RenderedImage image) {
        entries.put(key, image);
    }

    @Override
    public synchronized void clear() {
        entries.clear();
    }

    synchronized int size() {
        return entries.size();
    }
}
