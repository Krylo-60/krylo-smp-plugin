package com.krylo.smp.listeners;

import com.krylo.smp.KryloSMP;
import com.krylo.smp.spawn.SpawnGenerator;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Handles player join/quit events with welcome titles, chat messages,
 * and instant teleportation to the Neon Velocity spawn platform.
 */
public class JoinListener implements Listener {

    private final KryloSMP plugin;

    public JoinListener(KryloSMP plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.incrementJoins();

        // Initialize economy for new players
        if (plugin.getBalance(player.getUniqueId()) == plugin.getConfig().getDouble("starting-balance", 500.0)) {
            plugin.setBalance(player.getUniqueId(), plugin.getConfig().getDouble("starting-balance", 500.0));
        }

        // ── Instant Teleport to Spawn Platform ──────────────
        SpawnGenerator gen = plugin.getSpawnGenerator();
        if (gen != null) {
            // Delay 1 tick to ensure player is fully loaded into world
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                Location spawnLoc = gen.getSpawnLocation(
                    Bukkit.getWorlds().get(0)
                );
                player.teleport(spawnLoc);
                player.playSound(spawnLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 0.6f, 1.4f);
            }, 5L);
        }

        boolean welcomeEnabled = plugin.getConfig().getBoolean("welcome.enabled", true);
        if (!welcomeEnabled) return;

        // Send title and subtitle
        String title = colorize(plugin.getConfig().getString("welcome.title", "&b⚡ &fWelcome to &6KryloSMP &b⚡"));
        String subtitle = colorize(plugin.getConfig().getString("welcome.subtitle", "&7Built by Krishiv — Have fun!"));
        player.sendTitle(title, subtitle, 10, 70, 20);

        // Custom join message
        String chatMsg = colorize(plugin.getConfig().getString("welcome.chat-message", "&8[&6KryloSMP&8] &aWelcome &e{player} &ato the server!"))
            .replace("{player}", player.getName())
            .replace("{count}", String.valueOf(plugin.getTotalJoins()));

        event.setJoinMessage(chatMsg);

        // Play a sound to the joining player
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.2f);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        event.setQuitMessage(ChatColor.GRAY + "« " + ChatColor.RED + event.getPlayer().getName() + ChatColor.GRAY + " left the server");
    }

    private String colorize(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
