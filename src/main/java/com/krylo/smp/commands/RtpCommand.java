package com.krylo.smp.commands;

import com.krylo.smp.KryloSMP;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * /rtp (Random Teleport) — Teleports the player to a random safe
 * location in the overworld. Has a 30-second cooldown to prevent spam.
 */
public class RtpCommand implements CommandExecutor {

    private final KryloSMP plugin;
    private final Random random = new Random();
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    private static final int COOLDOWN_SECONDS = 30;
    private static final int MIN_RANGE = 500;
    private static final int MAX_RANGE = 5000;
    private static final int MAX_ATTEMPTS = 15;

    public RtpCommand(KryloSMP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }

        Player player = (Player) sender;

        // Check cooldown
        if (cooldowns.containsKey(player.getUniqueId())) {
            long elapsed = (System.currentTimeMillis() - cooldowns.get(player.getUniqueId())) / 1000;
            if (elapsed < COOLDOWN_SECONDS) {
                long remaining = COOLDOWN_SECONDS - elapsed;
                player.sendMessage(ChatColor.RED + "⏳ Random TP is on cooldown! Wait " +
                    ChatColor.YELLOW + remaining + "s" + ChatColor.RED + " before using again.");
                return true;
            }
        }

        player.sendMessage(ChatColor.AQUA + "⚡ " + ChatColor.WHITE + "Finding a safe random location...");

        // Run async-safe: find location then teleport on main thread
        World world = Bukkit.getWorlds().get(0); // Overworld

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Location safeLoc = findSafeLocation(world);

            // Teleport must happen on the main thread
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (safeLoc != null) {
                    // Load the chunk first
                    safeLoc.getChunk().load(true);

                    // Apply premium sci-fi warp effects
                    performWarpEffects(player, safeLoc);

                    cooldowns.put(player.getUniqueId(), System.currentTimeMillis());

                    player.sendMessage("");
                    player.sendMessage(ChatColor.GREEN + "✓ " + ChatColor.WHITE + "Randomly teleported to " +
                        ChatColor.AQUA + "(" + safeLoc.getBlockX() + ", " +
                        safeLoc.getBlockY() + ", " + safeLoc.getBlockZ() + ")");
                    player.sendMessage(ChatColor.GRAY + "  Explore and build your base here! Use " +
                        ChatColor.YELLOW + "/sethome" + ChatColor.GRAY + " to save this location.");
                    player.sendMessage("");
                } else {
                    player.sendMessage(ChatColor.RED + "❌ Couldn't find a safe location. Try again!");
                }
            });
        });

        return true;
    }

    /**
     * Triggers glowing particles, layered warp sounds, and screen effects.
     */
    public void performWarpEffects(Player player, Location safeLoc) {
        player.teleport(safeLoc);

        // Play dual warp sounds (layered)
        player.playSound(safeLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
        player.playSound(safeLoc, Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.5f);

        // Spawn a blast of portal particles
        player.getWorld().spawnParticle(Particle.PORTAL, safeLoc.clone().add(0, 1, 0), 100, 0.5, 1.0, 0.5, 0.1);

        // Apply visual screen warp effects (Nausea & Blindness)
        player.addPotionEffect(new org.bukkit.potion.PotionEffect(
            org.bukkit.potion.PotionEffectType.NAUSEA, 40, 1));
        player.addPotionEffect(new org.bukkit.potion.PotionEffect(
            org.bukkit.potion.PotionEffectType.BLINDNESS, 20, 1));
    }

    /**
     * Finds a safe random location: solid ground, no lava, no water on top.
     */
    private Location findSafeLocation(World world) {
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            int x = randomCoord();
            int z = randomCoord();

            // Get highest block (thread-safe read)
            int y = world.getHighestBlockYAt(x, z);

            // Skip if too low (void) or too high
            if (y < 10 || y > 250) continue;

            Block ground = world.getBlockAt(x, y, z);
            Block feet = world.getBlockAt(x, y + 1, z);
            Block head = world.getBlockAt(x, y + 2, z);

            // Check ground is solid
            if (!ground.getType().isSolid()) continue;

            // No lava or fire
            if (ground.getType() == Material.LAVA || ground.getType() == Material.MAGMA_BLOCK) continue;

            // Feet and head must be air (safe to stand)
            if (feet.getType() != Material.AIR && feet.getType() != Material.CAVE_AIR) continue;
            if (head.getType() != Material.AIR && head.getType() != Material.CAVE_AIR) continue;

            // No spawning in water
            if (ground.getType() == Material.WATER) continue;

            // Safe! Return location centered on block
            return new Location(world, x + 0.5, y + 1, z + 0.5);
        }
        return null;
    }

    /**
     * Generates a random coordinate within the configured range,
     * either positive or negative from spawn.
     */
    private int randomCoord() {
        int range = MIN_RANGE + random.nextInt(MAX_RANGE - MIN_RANGE);
        return random.nextBoolean() ? range : -range;
    }
}
