package dev.frameart.bukkit.version;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServerVersionTest {

    @Test
    void legacyVersions() {
        assertTrue(ServerVersion.parse("1.8.8-R0.1-SNAPSHOT").isLegacy());
        assertTrue(ServerVersion.parse("1.12.2-R0.1-SNAPSHOT").isLegacy());
    }

    @Test
    void modernVersions() {
        assertFalse(ServerVersion.parse("1.13.2-R0.1-SNAPSHOT").isLegacy());
        assertFalse(ServerVersion.parse("1.21.4-R0.1-SNAPSHOT").isLegacy());
    }

    @Test
    void yearBasedVersions() {
        ServerVersion version = ServerVersion.parse("26.2-R0.1-SNAPSHOT");
        assertEquals(26, version.major());
        assertEquals(2, version.minor());
        assertFalse(version.isLegacy());
        assertEquals("26.2", version.display());
    }
}
