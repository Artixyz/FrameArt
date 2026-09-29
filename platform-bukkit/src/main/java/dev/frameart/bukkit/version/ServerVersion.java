package dev.frameart.bukkit.version;

/**
 * Versão do Minecraft a partir de {@code Bukkit.getBukkitVersion()}.
 * Entende o esquema antigo ("1.8.8-R0.1-SNAPSHOT") e o novo por ano ("26.2-R0.1-SNAPSHOT").
 */
public record ServerVersion(int major, int minor, int patch, String raw) {

    public static ServerVersion parse(String bukkitVersion) {
        String core = bukkitVersion.split("-", 2)[0];
        String[] parts = core.split("\\.");
        return new ServerVersion(number(parts, 0), number(parts, 1), number(parts, 2), bukkitVersion);
    }

    public boolean isAtLeast(int major, int minor) {
        return this.major != major ? this.major > major : this.minor >= minor;
    }

    /** 1.8 – 1.12.2: IDs de mapa em short e sem FILLED_MAP. */
    public boolean isLegacy() {
        return !isAtLeast(1, 13);
    }

    public String display() {
        return patch > 0 ? major + "." + minor + "." + patch : major + "." + minor;
    }

    private static int number(String[] parts, int index) {
        if (index >= parts.length) {
            return 0;
        }
        String digits = parts[index].replaceAll("\\D.*$", "");
        return digits.isEmpty() ? 0 : Integer.parseInt(digits);
    }
}
