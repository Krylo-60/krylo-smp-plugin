package com.krylo.smp.effects;

import com.krylo.smp.KryloSMP;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class CapeTrail {

    private final KryloSMP plugin;

    public CapeTrail(KryloSMP plugin) {
        this.plugin = plugin;
        startTrailTask();
    }

    private void startTrailTask() {
        // Run every 2 ticks (10 times a second) for smooth trails without lagging the server
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                String choice = plugin.getConfig().getString("cape-selections." + player.getUniqueId().toString(), "");
                if ("NOW".equalsIgnoreCase(choice)) {
                    // Check if player is moving
                    if (player.getVelocity().lengthSquared() > 0.001) {
                        spawnNeonTrail(player);
                    }
                }
            }
        }, 0L, 2L);
    }

    private void spawnNeonTrail(Player player) {
        Location loc = player.getLocation();
        Vector dir = loc.getDirection().normalize();
        
        // Position particles on the player's back (opposite of look direction)
        Location backLoc = loc.clone().subtract(dir.multiply(0.3)).add(0, 0.8, 0);

        // Neon Cyan color (0, 242, 255)
        Particle.DustOptions cyanDust = new Particle.DustOptions(Color.fromRGB(0, 242, 255), 0.8f);
        // Neon Green color (0, 255, 0)
        Particle.DustOptions greenDust = new Particle.DustOptions(Color.fromRGB(0, 255, 0), 0.8f);

        // Spawn a small spread of glowing particles
        player.getWorld().spawnParticle(Particle.DUST, backLoc, 3, 0.15, 0.3, 0.15, 0.02, cyanDust);
        player.getWorld().spawnParticle(Particle.DUST, backLoc, 2, 0.15, 0.3, 0.15, 0.02, greenDust);
    }
}
