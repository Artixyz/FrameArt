package dev.frameart.plugin;

import dev.frameart.adapter.legacy.LegacyVersionAdapter;
import dev.frameart.adapter.modern.ModernVersionAdapter;
import dev.frameart.bukkit.version.ServerVersion;
import dev.frameart.bukkit.version.VersionAdapter;

/** Escolhe o adaptador em tempo de execução; a classe não usada nunca é inicializada. */
final class VersionAdapterFactory {

    private VersionAdapterFactory() {
    }

    static VersionAdapter create(ServerVersion version) {
        return version.isLegacy() ? new LegacyVersionAdapter() : new ModernVersionAdapter();
    }
}
