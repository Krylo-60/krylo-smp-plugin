package com.krylo.smp.boss;

import com.krylo.smp.KryloSMP;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.WitherSkeleton;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CustomBossManager {

    private final KryloSMP plugin;
    private final Map<UUID, BossBar> activeBosses = new ConcurrentHashMap<>();

    public CustomBossManager(KryloSMP plugin) {
        this.plugin = plugin;
        startBossBarTracker();
    }

    public LivingEntity spawnWarlord(Location loc) {
        WitherSkeleton warlord = (WitherSkeleton) loc.getWorld().spawnEntity(loc, EntityType.WITHER_SKELETON);
        warlord.setCustomName(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Warlord of Shadows");
        warlord.setCustomNameVisible(true);
        warlord.setMetadata("KryloSMPBoss", new FixedMetadataValue(plugin, "warlord"));

        // Setup boss attributes
        warlord.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(300.0);
        warlord.setHealth(300.0);
        warlord.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.35);
        warlord.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(12.0);

        // Equipment
        warlord.getEquipment().setItemInMainHand(new ItemStack(Material.NETHERITE_SWORD));
        warlord.getEquipment().setItemInMainHandDropChance(0.0f);

        // Create BossBar
        BossBar bar = Bukkit.createBossBar(
                ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Warlord of Shadows",
                BarColor.PURPLE,
                BarStyle.SOLID
        );
        activeBosses.put(warlord.getUniqueId(), bar);

        return warlord;
    }

    public LivingEntity spawnCherryElder(Location loc) {
        IronGolem elder = (IronGolem) loc.getWorld().spawnEntity(loc, EntityType.IRON_GOLEM);
        elder.setCustomName(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Cherry Elder");
        elder.setCustomNameVisible(true);
        elder.setMetadata("KryloSMPBoss", new FixedMetadataValue(plugin, "elder"));

        // Setup boss attributes
        elder.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(400.0);
        elder.setHealth(400.0);
        elder.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.3);
        elder.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(15.0);

        // Create BossBar
        BossBar bar = Bukkit.createBossBar(
                ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Cherry Elder",
                BarColor.PINK,
                BarStyle.SOLID
        );
        activeBosses.put(elder.getUniqueId(), bar);

        return elder;
    }

    public boolean isActiveBoss(UUID uuid) {
        return activeBosses.containsKey(uuid);
    }

    public BossBar getBossBar(UUID uuid) {
        return activeBosses.get(uuid);
    }

    public void removeBoss(UUID uuid) {
        BossBar bar = activeBosses.remove(uuid);
        if (bar != null) {
            bar.removeAll();
        }
    }

    public void cleanup() {
        for (BossBar bar : activeBosses.values()) {
            bar.removeAll();
        }
        activeBosses.clear();
    }

    private void startBossBarTracker() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Map.Entry<UUID, BossBar> entry : activeBosses.entrySet()) {
                    UUID uuid = entry.getKey();
                    BossBar bar = entry.getValue();
                    LivingEntity entity = (LivingEntity) Bukkit.getEntity(uuid);

                    if (entity == null || entity.isDead()) {
                        removeBoss(uuid);
                        continue;
                    }

                    // Update progress
                    double max = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
                    double current = entity.getHealth();
                    bar.setProgress(Math.max(0.0, Math.min(1.0, current / max)));

                    // Add/Remove nearby players
                    List<Player> nearby = new ArrayList<>();
                    for (Player p : entity.getWorld().getPlayers()) {
                        if (p.getLocation().distanceSquared(entity.getLocation()) < 40 * 40) {
                            nearby.add(p);
                        }
                    }

                    // Add new players
                    for (Player p : nearby) {
                        if (!bar.getPlayers().contains(p)) {
                            bar.addPlayer(p);
                        }
                    }

                    // Remove far players
                    List<Player> toRemove = new ArrayList<>();
                    for (Player p : bar.getPlayers()) {
                        if (!nearby.contains(p)) {
                            toRemove.add(p);
                        }
                    }
                    for (Player p : toRemove) {
                        bar.removePlayer(p);
                    }
                }
            }
        }.runTaskTimer(plugin, 10L, 10L); // Check every 0.5s
    }

    // Custom Drops Getter
    public List<ItemStack> getBossDrops(String bossType) {
        List<ItemStack> drops = new ArrayList<>();
        if ("warlord".equalsIgnoreCase(bossType)) {
            ItemStack core = new ItemStack(Material.HEART_OF_THE_SEA);
            ItemMeta meta = core.getItemMeta();
            meta.setDisplayName(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Shadow Core");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "A dark pulsating core containing shadow energy.");
            lore.add(ChatColor.DARK_GRAY + "Used to craft the Shadow Blade or Vampiric Dagger.");
            meta.setLore(lore);
            core.setItemMeta(meta);
            drops.add(core);
        } else if ("elder".equalsIgnoreCase(bossType)) {
            ItemStack blossom = new ItemStack(Material.PINK_PETALS);
            ItemMeta meta = blossom.getItemMeta();
            meta.setDisplayName(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Elder Blossom");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "An ancient, highly resilient blossom petal.");
            lore.add(ChatColor.DARK_GRAY + "Used to craft the Blossom Shield.");
            meta.setLore(lore);
            blossom.setItemMeta(meta);
            drops.add(blossom);
        }
        return drops;
    }
}
