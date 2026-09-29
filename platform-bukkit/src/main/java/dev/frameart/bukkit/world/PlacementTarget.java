package dev.frameart.bukkit.world;

import dev.frameart.core.domain.BlockPosition;
import dev.frameart.core.domain.Facing;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.Set;

/**
 * Onde a imagem começa: o bloco em frente à parede que o jogador está olhando.
 * Esse bloco vira o canto inferior esquerdo da imagem.
 */
public record PlacementTarget(BlockPosition origin, Facing facing) {

    public static Optional<PlacementTarget> of(Player player, int maxDistance) {
        Block wall = player.getTargetBlock((Set<Material>) null, maxDistance);
        if (wall == null || !wall.getType().isSolid()) {
            return Optional.empty();
        }
        Facing frameFacing = BukkitConversions.facingFromYaw(player.getLocation().getYaw()).opposite();
        Block front = wall.getRelative(BukkitConversions.toBlockFace(frameFacing));
        return Optional.of(new PlacementTarget(BukkitConversions.toPosition(front), frameFacing));
    }
}
