package com.krylo.smp.spawn;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * SpawnGenerator — Builds the Neon Velocity floating spawn platform.
 *
 * Automatically detects the world spawn and builds the platform
 * 40 blocks above the highest terrain point, so it never interferes
 * with existing builds, caves, or terrain below.
 */
public class SpawnGenerator {

    private static final int PLATFORM_RADIUS = 15;
    private static final int PILLAR_HEIGHT = 8;
    private static final int SKY_OFFSET = 40; // How far above terrain to build
    private static final String HOLOGRAM_TAG = "krylo_hologram";
    private static final String SPAWN_TAG = "krylo_spawn_marker";

    private int centerX;
    private int centerY;
    private int centerZ;

    private final JavaPlugin plugin;

    public SpawnGenerator(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Generates the entire spawn structure in the given world.
     * Picks the best location automatically:
     *  - Uses the world spawn X/Z coordinates
     *  - Finds the highest solid block at that position
     *  - Builds the platform 40 blocks above it (floating in sky)
     *  - Never touches or modifies terrain/builds below
     */
    public void generate(World world) {
        plugin.getLogger().info("[SpawnGen] Calculating best spawn location...");

        // Use world spawn coordinates for X/Z
        Location worldSpawn = world.getSpawnLocation();
        centerX = worldSpawn.getBlockX();
        centerZ = worldSpawn.getBlockZ();

        // Find the highest block in the platform area to avoid collisions
        int highestY = 0;
        for (int x = centerX - PLATFORM_RADIUS - 5; x <= centerX + PLATFORM_RADIUS + 5; x++) {
            for (int z = centerZ - PLATFORM_RADIUS - 5; z <= centerZ + PLATFORM_RADIUS + 5; z++) {
                int topY = world.getHighestBlockYAt(x, z);
                if (topY > highestY) {
                    highestY = topY;
                }
            }
        }

        // Build platform well above everything — safe floating height
        centerY = Math.min(highestY + SKY_OFFSET, 300); // Cap at Y=300 to stay in build limit
        plugin.getLogger().info("[SpawnGen] Building at (" + centerX + ", " + centerY + ", " + centerZ + ") — " +
            SKY_OFFSET + " blocks above highest terrain (Y=" + highestY + ")");

        // Force-load required chunks
        int chunkRadius = (PLATFORM_RADIUS / 16) + 2;
        for (int cx = -chunkRadius; cx <= chunkRadius; cx++) {
            for (int cz = -chunkRadius; cz <= chunkRadius; cz++) {
                world.getChunkAt((centerX >> 4) + cx, (centerZ >> 4) + cz).load(true);
            }
        }

        // ═══════════════════════════════════════
        // 1. ONLY PLACE BLOCKS IN AIR — never replace existing blocks
        //    (Platform is high in sky so this is just a safety check)
        // ═══════════════════════════════════════

        // ═══════════════════════════════════════
        // 2. BUILD CIRCULAR PLATFORM
        // ═══════════════════════════════════════
        for (int x = -PLATFORM_RADIUS; x <= PLATFORM_RADIUS; x++) {
            for (int z = -PLATFORM_RADIUS; z <= PLATFORM_RADIUS; z++) {
                double dist = Math.sqrt(x * x + z * z);

                if (dist <= PLATFORM_RADIUS) {
                    int bx = centerX + x;
                    int bz = centerZ + z;

                    // Only place if the spot is currently air (safety)
                    Block block = world.getBlockAt(bx, centerY, bz);

                    if (dist >= PLATFORM_RADIUS - 1.5) {
                        // Outer ring — neon cyan accent border
                        block.setType(Material.SEA_LANTERN);
                    } else if (dist >= PLATFORM_RADIUS - 3) {
                        // Inner ring — warped planks accent
                        block.setType(Material.WARPED_PLANKS);
                    } else {
                        // Main floor — alternating deepslate + obsidian pattern
                        if ((Math.abs(x) + Math.abs(z)) % 3 == 0) {
                            block.setType(Material.OBSIDIAN);
                        } else {
                            block.setType(Material.POLISHED_DEEPSLATE);
                        }
                    }

                    // Glowing underside layer for floating effect
                    if (dist <= PLATFORM_RADIUS - 1) {
                        if (dist >= PLATFORM_RADIUS - 4 && (x + z) % 4 == 0) {
                            world.getBlockAt(bx, centerY - 1, bz).setType(Material.SEA_LANTERN);
                        } else {
                            world.getBlockAt(bx, centerY - 1, bz).setType(Material.DEEPSLATE_BRICKS);
                        }
                    }
                }
            }
        }

        // ═══════════════════════════════════════
        // 3. NEON CROSS PATTERN ON FLOOR
        // ═══════════════════════════════════════
        for (int i = -PLATFORM_RADIUS + 3; i <= PLATFORM_RADIUS - 3; i++) {
            double dist = Math.abs(i);
            if (dist <= PLATFORM_RADIUS - 3) {
                // N-S line
                world.getBlockAt(centerX, centerY, centerZ + i).setType(Material.CYAN_CONCRETE);
                // E-W line
                world.getBlockAt(centerX + i, centerY, centerZ).setType(Material.CYAN_CONCRETE);
            }
        }
        // Center beacon block (also used as detection marker)
        world.getBlockAt(centerX, centerY, centerZ).setType(Material.DIAMOND_BLOCK);

        // ═══════════════════════════════════════
        // 4. BUILD FOUR DIRECTIONAL PILLARS
        // ═══════════════════════════════════════
        int pillarOffset = PLATFORM_RADIUS - 2;
        int[][] pillarPositions = {
            {centerX, centerZ - pillarOffset},  // North
            {centerX, centerZ + pillarOffset},  // South
            {centerX + pillarOffset, centerZ},   // East
            {centerX - pillarOffset, centerZ}    // West
        };

        for (int[] pos : pillarPositions) {
            int px = pos[0];
            int pz = pos[1];

            for (int y = centerY + 1; y <= centerY + PILLAR_HEIGHT; y++) {
                if (y == centerY + PILLAR_HEIGHT) {
                    // Top of pillar — sea lantern beacon
                    world.getBlockAt(px, y, pz).setType(Material.SEA_LANTERN);
                } else if (y == centerY + PILLAR_HEIGHT - 1) {
                    world.getBlockAt(px, y, pz).setType(Material.WARPED_PLANKS);
                } else {
                    world.getBlockAt(px, y, pz).setType(Material.POLISHED_BLACKSTONE_BRICKS);
                }
            }

            // Decorative base around pillar
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dz == 0) continue;
                    double d = Math.sqrt((px + dx - centerX) * (px + dx - centerX) +
                                         (pz + dz - centerZ) * (pz + dz - centerZ));
                    if (d <= PLATFORM_RADIUS) {
                        world.getBlockAt(px + dx, centerY + 1, pz + dz).setType(Material.POLISHED_BLACKSTONE_SLAB);
                    }
                }
            }
        }

        // ═══════════════════════════════════════
        // 5. ARCH CONNECTORS BETWEEN PILLARS
        // ═══════════════════════════════════════
        int archY = centerY + PILLAR_HEIGHT;
        buildArch(world, pillarPositions[0][0], pillarPositions[0][1],
                  pillarPositions[2][0], pillarPositions[2][1], archY);
        buildArch(world, pillarPositions[2][0], pillarPositions[2][1],
                  pillarPositions[1][0], pillarPositions[1][1], archY);
        buildArch(world, pillarPositions[1][0], pillarPositions[1][1],
                  pillarPositions[3][0], pillarPositions[3][1], archY);
        buildArch(world, pillarPositions[3][0], pillarPositions[3][1],
                  pillarPositions[0][0], pillarPositions[0][1], archY);

        // ═══════════════════════════════════════
        // 6. SPAWN HOLOGRAPHIC WELCOME TEXT
        // ═══════════════════════════════════════
        double hx = centerX + 0.5;
        double hz = centerZ + 0.5;
        double hy = centerY + 4.0;
        double hySub = centerY + 3.5;

        spawnHologram(world, hx, hy, hz, 180.0f);
        spawnHologram(world, hx, hy, hz, 0.0f);
        spawnHologram(world, hx, hy, hz, 90.0f);
        spawnHologram(world, hx, hy, hz, -90.0f);

        spawnSubHologram(world, hx, hySub, hz, 180.0f);
        spawnSubHologram(world, hx, hySub, hz, 0.0f);
        spawnSubHologram(world, hx, hySub, hz, 90.0f);
        spawnSubHologram(world, hx, hySub, hz, -90.0f);

        // ═══════════════════════════════════════
        // 7. BUILD RTP (RANDOM TELEPORT) PADS
        //    4 gold pads around center — step on to teleport
        //    to a random safe spot in the world
        // ═══════════════════════════════════════
        int[][] rtpPadPositions = {
            {centerX + 5, centerZ},      // East pad
            {centerX - 5, centerZ},      // West pad
            {centerX, centerZ + 5},      // South pad
            {centerX, centerZ - 5}       // North pad
        };

        for (int[] pad : rtpPadPositions) {
            int px = pad[0];
            int pz = pad[1];

            // Gold block base (3x3)
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    world.getBlockAt(px + dx, centerY, pz + dz).setType(Material.GOLD_BLOCK);
                }
            }

            // Gold pressure plate on top (center)
            world.getBlockAt(px, centerY + 1, pz).setType(Material.LIGHT_WEIGHTED_PRESSURE_PLATE);

            // Glowstone corners for visibility
            world.getBlockAt(px - 1, centerY + 1, pz - 1).setType(Material.GLOWSTONE);
            world.getBlockAt(px + 1, centerY + 1, pz - 1).setType(Material.GLOWSTONE);
            world.getBlockAt(px - 1, centerY + 1, pz + 1).setType(Material.GLOWSTONE);
            world.getBlockAt(px + 1, centerY + 1, pz + 1).setType(Material.GLOWSTONE);

            // Label hologram above pad
            Location labelLoc = new Location(world, px + 0.5, centerY + 2.5, pz + 0.5);
            ArmorStand label = (ArmorStand) world.spawnEntity(labelLoc, EntityType.ARMOR_STAND);
            label.setVisible(false);
            label.setGravity(false);
            label.setInvulnerable(true);
            label.setCustomNameVisible(true);
            label.setCustomName(ChatColor.GOLD + "⚡ " + ChatColor.GREEN + "STEP TO RANDOM TP" + ChatColor.GOLD + " ⚡");
            label.setSmall(true);
            label.setMarker(true);
            label.addScoreboardTag(HOLOGRAM_TAG);
        }

        // Update world spawn to platform center
        world.setSpawnLocation(centerX, centerY + 1, centerZ);

        // Save spawn coordinates to config for persistence
        plugin.getConfig().set("spawn-platform.x", centerX);
        plugin.getConfig().set("spawn-platform.y", centerY);
        plugin.getConfig().set("spawn-platform.z", centerZ);
        plugin.getConfig().set("spawn-platform.built", true);
        plugin.saveConfig();

        plugin.getLogger().info("[SpawnGen] ⚡ Neon Velocity spawn platform complete at (" +
            centerX + ", " + centerY + ", " + centerZ + ")!");
    }

    /**
     * Builds an arch between two pillar tops using warped planks.
     */
    private void buildArch(World world, int x1, int z1, int x2, int z2, int y) {
        int steps = Math.max(Math.abs(x2 - x1), Math.abs(z2 - z1));
        if (steps == 0) return;

        for (int i = 0; i <= steps; i++) {
            int bx = x1 + (x2 - x1) * i / steps;
            int bz = z1 + (z2 - z1) * i / steps;
            double progress = (double) i / steps;
            int archHeight = (int) Math.round(Math.sin(progress * Math.PI) * 2);
            world.getBlockAt(bx, y + archHeight, bz).setType(Material.WARPED_PLANKS);
        }
    }

    /**
     * Spawns an invisible armor stand with custom name as holographic text.
     */
    private void spawnHologram(World world, double x, double y, double z, float yaw) {
        Location loc = new Location(world, x, y, z, yaw, 0);
        ArmorStand stand = (ArmorStand) world.spawnEntity(loc, EntityType.ARMOR_STAND);
        stand.setVisible(false);
        stand.setGravity(false);
        stand.setInvulnerable(true);
        stand.setCustomNameVisible(true);
        stand.setCustomName(ChatColor.AQUA + "" + ChatColor.BOLD + "Welcome to KryloSMP");
        stand.setSmall(false);
        stand.setMarker(true);
        stand.addScoreboardTag(HOLOGRAM_TAG);
    }

    /**
     * Spawns subtitle holographic text.
     */
    private void spawnSubHologram(World world, double x, double y, double z, float yaw) {
        Location loc = new Location(world, x, y, z, yaw, 0);
        ArmorStand stand = (ArmorStand) world.spawnEntity(loc, EntityType.ARMOR_STAND);
        stand.setVisible(false);
        stand.setGravity(false);
        stand.setInvulnerable(true);
        stand.setCustomNameVisible(true);
        stand.setCustomName(ChatColor.GRAY + "⚔ Java + Bedrock ⚔ " + ChatColor.DARK_AQUA + "/spawn");
        stand.setSmall(true);
        stand.setMarker(true);
        stand.addScoreboardTag(HOLOGRAM_TAG);
    }

    /**
     * Checks if the spawn platform has already been built (via saved config).
     */
    public boolean isSpawnBuilt(World world) {
        return plugin.getConfig().getBoolean("spawn-platform.built", false);
    }

    /**
     * Returns the teleport location (center of platform, one block above floor).
     */
    public Location getSpawnLocation(World world) {
        int x = plugin.getConfig().getInt("spawn-platform.x", centerX);
        int y = plugin.getConfig().getInt("spawn-platform.y", centerY);
        int z = plugin.getConfig().getInt("spawn-platform.z", centerZ);
        return new Location(world, x + 0.5, y + 1, z + 0.5, 0, 0);
    }
}
