package com.krylo.smp.spawn;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * SpawnGenerator — Builds the Neon Velocity floating spawn platform.
 *
 * Creates a circular polished deepslate/obsidian platform at (0, 100, 0)
 * with neon cyan accents, four directional pillars, and holographic
 * welcome text displays facing all four cardinal directions.
 */
public class SpawnGenerator {

    private static final int CENTER_X = 0;
    private static final int CENTER_Y = 100;
    private static final int CENTER_Z = 0;
    private static final int PLATFORM_RADIUS = 15;
    private static final int PILLAR_HEIGHT = 8;

    private final JavaPlugin plugin;

    public SpawnGenerator(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Generates the entire spawn structure in the given world.
     * Safe for chunk loading — forces chunk load before placing blocks.
     */
    public void generate(World world) {
        plugin.getLogger().info("[SpawnGen] Building Neon Velocity spawn platform...");

        // Force-load required chunks
        int chunkRadius = (PLATFORM_RADIUS / 16) + 2;
        for (int cx = -chunkRadius; cx <= chunkRadius; cx++) {
            for (int cz = -chunkRadius; cz <= chunkRadius; cz++) {
                world.getChunkAt((CENTER_X >> 4) + cx, (CENTER_Z >> 4) + cz).load(true);
            }
        }

        // ═══════════════════════════════════════
        // 1. CLEAR AREA BELOW AND ABOVE PLATFORM
        // ═══════════════════════════════════════
        for (int x = CENTER_X - PLATFORM_RADIUS - 2; x <= CENTER_X + PLATFORM_RADIUS + 2; x++) {
            for (int z = CENTER_Z - PLATFORM_RADIUS - 2; z <= CENTER_Z + PLATFORM_RADIUS + 2; z++) {
                for (int y = CENTER_Y - 1; y <= CENTER_Y + PILLAR_HEIGHT + 5; y++) {
                    world.getBlockAt(x, y, z).setType(Material.AIR);
                }
            }
        }

        // ═══════════════════════════════════════
        // 2. BUILD CIRCULAR PLATFORM
        // ═══════════════════════════════════════
        for (int x = -PLATFORM_RADIUS; x <= PLATFORM_RADIUS; x++) {
            for (int z = -PLATFORM_RADIUS; z <= PLATFORM_RADIUS; z++) {
                double dist = Math.sqrt(x * x + z * z);

                if (dist <= PLATFORM_RADIUS) {
                    int bx = CENTER_X + x;
                    int bz = CENTER_Z + z;

                    if (dist >= PLATFORM_RADIUS - 1.5) {
                        // Outer ring — neon cyan accent border
                        world.getBlockAt(bx, CENTER_Y, bz).setType(Material.SEA_LANTERN);
                    } else if (dist >= PLATFORM_RADIUS - 3) {
                        // Inner ring — warped planks accent
                        world.getBlockAt(bx, CENTER_Y, bz).setType(Material.WARPED_PLANKS);
                    } else {
                        // Main floor — alternating deepslate + obsidian pattern
                        if ((Math.abs(x) + Math.abs(z)) % 3 == 0) {
                            world.getBlockAt(bx, CENTER_Y, bz).setType(Material.OBSIDIAN);
                        } else {
                            world.getBlockAt(bx, CENTER_Y, bz).setType(Material.POLISHED_DEEPSLATE);
                        }
                    }

                    // Glowing underside layer for floating effect
                    if (dist <= PLATFORM_RADIUS - 1) {
                        if (dist >= PLATFORM_RADIUS - 4 && (x + z) % 4 == 0) {
                            world.getBlockAt(bx, CENTER_Y - 1, bz).setType(Material.SEA_LANTERN);
                        } else {
                            world.getBlockAt(bx, CENTER_Y - 1, bz).setType(Material.DEEPSLATE_BRICKS);
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
                world.getBlockAt(CENTER_X, CENTER_Y, CENTER_Z + i).setType(Material.CYAN_CONCRETE);
                // E-W line
                world.getBlockAt(CENTER_X + i, CENTER_Y, CENTER_Z).setType(Material.CYAN_CONCRETE);
            }
        }
        // Center beacon block
        world.getBlockAt(CENTER_X, CENTER_Y, CENTER_Z).setType(Material.DIAMOND_BLOCK);

        // ═══════════════════════════════════════
        // 4. BUILD FOUR DIRECTIONAL PILLARS
        // ═══════════════════════════════════════
        int pillarOffset = PLATFORM_RADIUS - 2;
        int[][] pillarPositions = {
            {CENTER_X, CENTER_Z - pillarOffset},  // North
            {CENTER_X, CENTER_Z + pillarOffset},  // South
            {CENTER_X + pillarOffset, CENTER_Z},   // East
            {CENTER_X - pillarOffset, CENTER_Z}    // West
        };

        for (int[] pos : pillarPositions) {
            int px = pos[0];
            int pz = pos[1];

            for (int y = CENTER_Y + 1; y <= CENTER_Y + PILLAR_HEIGHT; y++) {
                if (y == CENTER_Y + PILLAR_HEIGHT) {
                    // Top of pillar — sea lantern beacon
                    world.getBlockAt(px, y, pz).setType(Material.SEA_LANTERN);
                } else if (y == CENTER_Y + PILLAR_HEIGHT - 1) {
                    world.getBlockAt(px, y, pz).setType(Material.WARPED_PLANKS);
                } else {
                    world.getBlockAt(px, y, pz).setType(Material.POLISHED_BLACKSTONE_BRICKS);
                }
            }

            // Decorative base around pillar
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dz == 0) continue;
                    double d = Math.sqrt((px + dx - CENTER_X) * (px + dx - CENTER_X) +
                                         (pz + dz - CENTER_Z) * (pz + dz - CENTER_Z));
                    if (d <= PLATFORM_RADIUS) {
                        world.getBlockAt(px + dx, CENTER_Y + 1, pz + dz).setType(Material.POLISHED_BLACKSTONE_SLAB);
                    }
                }
            }
        }

        // ═══════════════════════════════════════
        // 5. ARCH CONNECTORS BETWEEN PILLARS
        // ═══════════════════════════════════════
        int archY = CENTER_Y + PILLAR_HEIGHT;
        // North-East arch
        buildArch(world, pillarPositions[0][0], pillarPositions[0][1],
                  pillarPositions[2][0], pillarPositions[2][1], archY);
        // East-South arch
        buildArch(world, pillarPositions[2][0], pillarPositions[2][1],
                  pillarPositions[1][0], pillarPositions[1][1], archY);
        // South-West arch
        buildArch(world, pillarPositions[1][0], pillarPositions[1][1],
                  pillarPositions[3][0], pillarPositions[3][1], archY);
        // West-North arch
        buildArch(world, pillarPositions[3][0], pillarPositions[3][1],
                  pillarPositions[0][0], pillarPositions[0][1], archY);

        // ═══════════════════════════════════════
        // 6. SPAWN HOLOGRAPHIC WELCOME TEXT
        // ═══════════════════════════════════════
        spawnHologram(world, CENTER_X + 0.5, CENTER_Y + 4.0, CENTER_Z + 0.5, 180.0f); // Facing South (toward +Z)
        spawnHologram(world, CENTER_X + 0.5, CENTER_Y + 4.0, CENTER_Z + 0.5, 0.0f);   // Facing North (toward -Z)
        spawnHologram(world, CENTER_X + 0.5, CENTER_Y + 4.0, CENTER_Z + 0.5, 90.0f);  // Facing West (toward -X)
        spawnHologram(world, CENTER_X + 0.5, CENTER_Y + 4.0, CENTER_Z + 0.5, -90.0f); // Facing East (toward +X)

        // Sub-text hologram
        spawnSubHologram(world, CENTER_X + 0.5, CENTER_Y + 3.5, CENTER_Z + 0.5, 180.0f);
        spawnSubHologram(world, CENTER_X + 0.5, CENTER_Y + 3.5, CENTER_Z + 0.5, 0.0f);
        spawnSubHologram(world, CENTER_X + 0.5, CENTER_Y + 3.5, CENTER_Z + 0.5, 90.0f);
        spawnSubHologram(world, CENTER_X + 0.5, CENTER_Y + 3.5, CENTER_Z + 0.5, -90.0f);

        // Set world spawn
        world.setSpawnLocation(CENTER_X, CENTER_Y + 1, CENTER_Z);

        plugin.getLogger().info("[SpawnGen] ⚡ Neon Velocity spawn platform complete!");
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
            // Gentle arch curve
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
        // Tag it so we can clean up later
        stand.addScoreboardTag("krylo_hologram");
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
        stand.addScoreboardTag("krylo_hologram");
    }

    /**
     * Checks if the spawn platform already exists.
     */
    public boolean isSpawnBuilt(World world) {
        Block centerBlock = world.getBlockAt(CENTER_X, CENTER_Y, CENTER_Z);
        return centerBlock.getType() == Material.DIAMOND_BLOCK;
    }

    /**
     * Returns the teleport location (center of platform, one block above).
     */
    public Location getSpawnLocation(World world) {
        return new Location(world, CENTER_X + 0.5, CENTER_Y + 1, CENTER_Z + 0.5, 0, 0);
    }
}
