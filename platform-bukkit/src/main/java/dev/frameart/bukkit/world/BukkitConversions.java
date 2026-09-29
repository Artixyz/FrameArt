package dev.frameart.bukkit.world;

import dev.frameart.core.domain.BlockPosition;
import dev.frameart.core.domain.Facing;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

/** Conversões entre tipos do domínio e do Bukkit. */
public final class BukkitConversions {

    private BukkitConversions() {
    }

    public static BlockFace toBlockFace(Facing facing) {
        return BlockFace.valueOf(facing.name());
    }

    public static BlockPosition toPosition(Block block) {
        return new BlockPosition(block.getWorld().getName(), block.getX(), block.getY(), block.getZ());
    }

    /** Direção cardinal para onde o jogador olha (yaw 0 = sul, 90 = oeste). */
    public static Facing facingFromYaw(float yaw) {
        float normalized = ((yaw % 360) + 360) % 360;
        if (normalized >= 45 && normalized < 135) {
            return Facing.WEST;
        }
        if (normalized >= 135 && normalized < 225) {
            return Facing.NORTH;
        }
        if (normalized >= 225 && normalized < 315) {
            return Facing.EAST;
        }
        return Facing.SOUTH;
    }
}
