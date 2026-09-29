package dev.frameart.bukkit.listener;

import dev.frameart.bukkit.version.VersionAdapter;
import dev.frameart.core.application.port.ManagedMapLookup;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import java.util.OptionalInt;

/** Impede quebrar, girar ou retirar mapas das molduras do plugin. Remoção só via comando. */
public final class FrameProtectionListener implements Listener {

    private final ManagedMapLookup lookup;
    private final VersionAdapter adapter;

    public FrameProtectionListener(ManagedMapLookup lookup, VersionAdapter adapter) {
        this.lookup = lookup;
        this.adapter = adapter;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onHangingBreak(HangingBreakEvent event) {
        if (isProtected(event.getEntity())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onFrameDamage(EntityDamageByEntityEvent event) {
        if (isProtected(event.getEntity())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onFrameInteract(PlayerInteractEntityEvent event) {
        if (isProtected(event.getRightClicked())) {
            event.setCancelled(true);
        }
    }

    private boolean isProtected(Entity entity) {
        if (!(entity instanceof ItemFrame frame)) {
            return false;
        }
        OptionalInt mapId = adapter.mapIdOf(frame.getItem());
        return mapId.isPresent() && lookup.isManaged(mapId.getAsInt());
    }
}
