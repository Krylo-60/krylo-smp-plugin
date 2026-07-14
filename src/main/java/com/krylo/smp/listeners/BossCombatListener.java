package com.krylo.smp.listeners;

import com.krylo.smp.KryloSMP;
import com.krylo.smp.boss.CustomBossManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BossCombatListener implements Listener {

    private final KryloSMP plugin;
    private final CustomBossManager bossManager;
    
    // Cooldown trackers
    private final Map<UUID, Long> leapCooldown = new HashMap<>();
    private final Map<UUID, Long> rootCooldown = new HashMap<>();
    private final Map<UUID, Boolean> minionTriggered = new HashMap<>();

    public BossCombatListener(KryloSMP plugin, CustomBossManager bossManager) {
        this.plugin = plugin;
        this.bossManager = bossManager;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBossDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof LivingEntity)) return;
        LivingEntity boss = (LivingEntity) event.getEntity();
        
        if (!boss.hasMetadata("KryloSMPBoss")) return;
        
        String bossType = getMetadataString(boss, "KryloSMPBoss");
        UUID bossUuid = boss.getUniqueId();

        // 1. Warlord Shadow Abilities
        if ("warlord".equalsIgnoreCase(bossType)) {
            // Trigger minion spawn at <50% HP
            double currentHp = boss.getHealth() - event.getFinalDamage();
            double maxHp = boss.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
            
            if (currentHp < (maxHp / 2) && !minionTriggered.getOrDefault(bossUuid, false)) {
                minionTriggered.put(bossUuid, true);
                spawnWarlordMinions(boss);
            }

            // Dark Leap check
            if (event.getDamager() instanceof Player) {
                Player attacker = (Player) event.getDamager();
                checkWarlordLeap(boss, attacker);
            }
        }

        // 2. Elder Abilities (Slam on attack)
        if (event.getDamager() instanceof LivingEntity) {
            LivingEntity damager = (LivingEntity) event.getDamager();
            if (damager.hasMetadata("KryloSMPBoss") && "elder".equalsIgnoreCase(getMetadataString(damager, "KryloSMPBoss"))) {
                // 25% chance to slam on hit
                if (Math.random() < 0.25 && event.getEntity() instanceof Player) {
                    Player victim = (Player) event.getEntity();
                    triggerElderSlam(damager, victim);
                }
            }
        }
    }

    @EventHandler
    public void onBossTarget(EntityDamageByEntityEvent event) {
        // Apply wither on warlord hits
        if (event.getDamager() instanceof LivingEntity && event.getEntity() instanceof LivingEntity) {
            LivingEntity attacker = (LivingEntity) event.getDamager();
            LivingEntity victim = (LivingEntity) event.getEntity();
            
            if (attacker.hasMetadata("KryloSMPBoss") && "warlord".equalsIgnoreCase(getMetadataString(attacker, "KryloSMPBoss"))) {
                victim.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 100, 1));
            }
        }
    }

    @EventHandler
    public void onBossDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (!entity.hasMetadata("KryloSMPBoss")) return;

        UUID uuid = entity.getUniqueId();
        String bossType = getMetadataString(entity, "KryloSMPBoss");

        // Clear default drops
        event.getDrops().clear();
        event.setDroppedExp(100); // 100 exp points

        // Drop boss-specific loot
        for (ItemStack drop : bossManager.getBossDrops(bossType)) {
            entity.getWorld().dropItemNaturally(entity.getLocation(), drop);
        }

        // Clean trackers
        leapCooldown.remove(uuid);
        rootCooldown.remove(uuid);
        minionTriggered.remove(uuid);
        bossManager.removeBoss(uuid);

        // Broadcast defeat
        String bossName = "warlord".equalsIgnoreCase(bossType) ? "Warlord of Shadows" : "Cherry Elder";
        org.bukkit.Bukkit.broadcastMessage(ChatColor.GOLD + "⚔️ " + ChatColor.RED + ChatColor.BOLD + bossName + ChatColor.YELLOW + " has been defeated! Rare relics have been dropped.");
    }

    private void checkWarlordLeap(LivingEntity warlord, Player target) {
        UUID uuid = warlord.getUniqueId();
        long now = System.currentTimeMillis();
        long lastLeap = leapCooldown.getOrDefault(uuid, 0L);

        if (now - lastLeap < 10000) return; // 10s cooldown

        double distSq = warlord.getLocation().distanceSquared(target.getLocation());
        if (distSq > 8 * 8 && distSq < 30 * 30) {
            leapCooldown.put(uuid, now);

            warlord.getWorld().playSound(warlord.getLocation(), Sound.ENTITY_WITHER_SHOOT, 1.0f, 0.8f);
            warlord.getWorld().spawnParticle(Particle.SMOKE, warlord.getLocation(), 30, 0.5, 1, 0.5, 0.1);

            // Teleport behind player with small offset
            Vector dir = target.getLocation().getDirection().normalize().multiply(-1.5); // 1.5 blocks behind
            Location leapLoc = target.getLocation().add(dir);
            leapLoc.setY(target.getLocation().getY());
            warlord.teleport(leapLoc);

            warlord.getWorld().playSound(warlord.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1.0f, 1.2f);
            warlord.getWorld().spawnParticle(Particle.PORTAL, warlord.getLocation(), 40, 0.5, 1, 0.5, 0.2);
            
            target.sendMessage(ChatColor.DARK_PURPLE + "⚡ The Warlord steps through the shadows behind you!");
        }
    }

    private void spawnWarlordMinions(LivingEntity warlord) {
        Location loc = warlord.getLocation();
        loc.getWorld().playSound(loc, Sound.ENTITY_WITHER_DEATH, 1.2f, 0.5f);
        loc.getWorld().spawnParticle(Particle.FLAME, loc, 50, 1.0, 1.0, 1.0, 0.1);

        org.bukkit.Bukkit.broadcastMessage(ChatColor.DARK_PURPLE + "👿 Warlord of Shadows: " + ChatColor.RED + "Arise, my dark soldiers! Tear them apart!");

        for (int i = 0; i < 3; i++) {
            double angle = (2 * Math.PI / 3) * i;
            double x = Math.cos(angle) * 3;
            double z = Math.sin(angle) * 3;
            Location minionLoc = loc.clone().add(x, 0.5, z);

            Skeleton minion = (Skeleton) loc.getWorld().spawnEntity(minionLoc, EntityType.SKELETON);
            minion.setCustomName(ChatColor.RED + "Warlord Minion");
            minion.setCustomNameVisible(true);
            minion.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).setBaseValue(40.0);
            minion.setHealth(40.0);
            minion.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 99999, 1));
            
            minion.getEquipment().setItemInMainHand(new ItemStack(Material.BOW));
        }
    }

    private void triggerElderSlam(LivingEntity elder, Player victim) {
        elder.getWorld().playSound(elder.getLocation(), Sound.ENTITY_IRON_GOLEM_ATTACK, 1.5f, 0.6f);
        elder.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, victim.getLocation(), 5, 0.5, 0.1, 0.5, 0.0);

        // Toss victim up
        victim.setVelocity(new Vector(0, 1.2, 0));
        victim.sendMessage(ChatColor.RED + "💥 The Cherry Elder slams the ground, sending you flying!");

        // Apply roots on a schedule if cooldown ready
        UUID uuid = elder.getUniqueId();
        long now = System.currentTimeMillis();
        long lastRoot = rootCooldown.getOrDefault(uuid, 0L);
        if (now - lastRoot > 12000) {
            rootCooldown.put(uuid, now);
            victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 2));
            victim.sendMessage(ChatColor.DARK_RED + "🕸️ You are rooted by ancient cherry blossoms!");
        }
    }

    private String getMetadataString(LivingEntity entity, String key) {
        for (MetadataValue val : entity.getMetadata(key)) {
            if (val.getOwningPlugin().getName().equals(plugin.getName())) {
                return val.asString();
            }
        }
        return "";
    }
}
