package com.krylo.smp.listeners;

import com.krylo.smp.KryloSMP;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.entity.Player;

/**
 * Protects the spawn area from block breaking/placing by non-admins.
 */
public class ProtectionListener implements Listener {

    private final KryloSMP plugin;

    public ProtectionListener(KryloSMP plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (isInSpawnZone(event.getBlock().getLocation()) && !event.getPlayer().hasPermission("krylosmp.admin")) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "⚠ You can't break blocks in the spawn area!");
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (isInSpawnZone(event.getBlock().getLocation()) && !event.getPlayer().hasPermission("krylosmp.admin")) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "⚠ You can't place blocks in the spawn area!");
        }
    }

    private boolean isInSpawnZone(Location loc) {
        if (!plugin.getConfig().contains("spawn.world")) return false;

        String spawnWorld = plugin.getConfig().getString("spawn.world");
        if (loc.getWorld() == null || !loc.getWorld().getName().equals(spawnWorld)) return false;

        double spawnX = plugin.getConfig().getDouble("spawn.x");
        double spawnZ = plugin.getConfig().getDouble("spawn.z");
        int radius = plugin.getConfig().getInt("spawn-protection-radius", 16);

        double dx = loc.getX() - spawnX;
        double dz = loc.getZ() - spawnZ;

        return (dx * dx + dz * dz) <= (radius * radius);
    }
}
