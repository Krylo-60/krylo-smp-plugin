package com.krylo.smp.listeners;

import com.krylo.smp.KryloSMP;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerWeaponListener implements Listener {

    private final KryloSMP plugin;
    private final Map<UUID, Long> shieldCooldown = new HashMap<>();
    private final Map<String, Long> weaponCooldowns = new HashMap<>();

    public PlayerWeaponListener(KryloSMP plugin) {
        this.plugin = plugin;
        startPassiveEffectsTracker();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onWeaponDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        Player attacker = (Player) event.getDamager();
        ItemStack hand = attacker.getInventory().getItemInMainHand();

        if (hand == null || !hand.hasItemMeta() || !hand.getItemMeta().hasDisplayName()) return;
        String name = hand.getItemMeta().getDisplayName();

        // 1. Shadow Blade Attack (10% Blindness)
        if (name.contains("Shadow Blade")) {
            if (event.getEntity() instanceof LivingEntity) {
                LivingEntity victim = (LivingEntity) event.getEntity();
                if (Math.random() < 0.10) {
                    victim.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 0));
                    victim.getWorld().spawnParticle(Particle.SMOKE, victim.getLocation().add(0, 1, 0), 10, 0.2, 0.2, 0.2, 0.0);
                    attacker.sendMessage(ChatColor.DARK_PURPLE + "⚡ Shadow Blade: Blinded target!");
                }
            }
        }

        // 2. Vampiric Dagger Attack (10% Lifesteal)
        if (name.contains("Vampiric Dagger")) {
            double damage = event.getFinalDamage();
            double healAmount = damage * 0.10;
            if (healAmount > 0.1) {
                double currentHealth = attacker.getHealth();
                double maxHealth = attacker.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
                attacker.setHealth(Math.min(maxHealth, currentHealth + healAmount));
                
                attacker.getWorld().spawnParticle(Particle.HEART, attacker.getLocation().add(0, 1.2, 0), 3, 0.1, 0.1, 0.1, 0.0);
                attacker.playSound(attacker.getLocation(), Sound.ENTITY_PLAYER_HURT_DROWN, 0.8f, 1.5f);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onWeaponInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Player player = event.getPlayer();
        ItemStack hand = player.getInventory().getItemInMainHand();

        if (hand == null || !hand.hasItemMeta() || !hand.getItemMeta().hasDisplayName()) return;
        String name = hand.getItemMeta().getDisplayName();
        String uuidStr = player.getUniqueId().toString();
        long now = System.currentTimeMillis();

        // 1. Godly Mace / Mjolnir (lightning strike)
        if (name.contains("Godly Mace of Krylo") || name.contains("Mjolnir Hammer")) {
            event.setCancelled(true);
            long lastUse = weaponCooldowns.getOrDefault(uuidStr + "_mjolnir", 0L);
            if (now - lastUse < 15000) {
                long remaining = 15 - (now - lastUse) / 1000;
                player.sendMessage(ChatColor.RED + "🔨 Mjolnir: Cooldown remaining " + remaining + "s");
                return;
            }
            weaponCooldowns.put(uuidStr + "_mjolnir", now);

            Block target = player.getTargetBlockExact(50);
            if (target != null) {
                player.getWorld().strikeLightning(target.getLocation());
                player.sendMessage(ChatColor.GOLD + "⚡ Mjolnir: Summoned lightning!");
            } else {
                player.sendMessage(ChatColor.RED + "No block in range!");
            }
        }

        // 2. Neptune Spear / Wind Spear (lunge + crash slam)
        if (name.contains("Neptune's God Spear") || name.contains("Spear of the Heavens")) {
            event.setCancelled(true);
            long lastUse = weaponCooldowns.getOrDefault(uuidStr + "_spear", 0L);
            if (now - lastUse < 12000) {
                long remaining = 12 - (now - lastUse) / 1000;
                player.sendMessage(ChatColor.RED + "🔱 Spear: Cooldown remaining " + remaining + "s");
                return;
            }
            weaponCooldowns.put(uuidStr + "_spear", now);

            Vector dir = player.getLocation().getDirection().multiply(2.2).setY(0.7);
            player.setVelocity(dir);
            player.playSound(player.getLocation(), Sound.ENTITY_WIND_CHARGE_THROW, 1.2f, 1.0f);
            player.getWorld().spawnParticle(Particle.GUST, player.getLocation(), 15, 0.5, 0.5, 0.5, 0.0);

            new BukkitRunnable() {
                int checks = 0;
                @Override
                public void run() {
                    checks++;
                    if (player.isOnGround() || checks > 30) {
                        player.getWorld().spawnParticle(Particle.EXPLOSION, player.getLocation(), 1, 0.0, 0.0, 0.0, 0.0);
                        player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.2f);
                        for (Entity e : player.getNearbyEntities(6.0, 4.0, 6.0)) {
                            if (e instanceof LivingEntity && e != player) {
                                LivingEntity le = (LivingEntity) e;
                                le.damage(6.0, player);
                                Vector kb = le.getLocation().toVector().subtract(player.getLocation().toVector()).normalize().multiply(1.5).setY(0.5);
                                le.setVelocity(kb);
                            }
                        }
                        player.sendMessage(ChatColor.AQUA + "🔱 Neptune's Spear: Ground Smash!");
                        cancel();
                    }
                }
            }.runTaskTimer(plugin, 5L, 2L);
        }

        // 3. Void Bow / Void Scythe (portal pull)
        if (name.contains("Void Bow") || name.contains("Scythe of Demeter")) {
            long lastUse = weaponCooldowns.getOrDefault(uuidStr + "_void", 0L);
            if (now - lastUse < 15000) {
                long remaining = 15 - (now - lastUse) / 1000;
                player.sendMessage(ChatColor.RED + "🌀 Void Pull: Cooldown remaining " + remaining + "s");
                return;
            }
            weaponCooldowns.put(uuidStr + "_void", now);

            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.2f, 0.8f);
            player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation(), 50, 1.0, 1.0, 1.0, 0.5);

            int pulled = 0;
            for (Entity e : player.getNearbyEntities(8.0, 8.0, 8.0)) {
                if (e instanceof LivingEntity && e != player) {
                    LivingEntity le = (LivingEntity) e;
                    Vector pull = player.getLocation().toVector().subtract(le.getLocation().toVector()).normalize().multiply(1.3).setY(0.4);
                    le.setVelocity(pull);
                    le.getWorld().spawnParticle(Particle.PORTAL, le.getLocation(), 15, 0.2, 0.2, 0.2, 0.1);
                    pulled++;
                }
            }
            if (pulled > 0) {
                player.sendMessage(ChatColor.DARK_PURPLE + "🌀 Void Pull: Pulled " + pulled + " enemies towards you!");
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onShieldBlock(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player victim = (Player) event.getEntity();

        if (victim.isBlocking()) {
            ItemStack shield = victim.getInventory().getItemInMainHand();
            if (shield == null || !shield.hasItemMeta() || !shield.getItemMeta().hasDisplayName()) {
                shield = victim.getInventory().getItemInOffHand();
            }

            if (shield != null && shield.hasItemMeta() && shield.getItemMeta().hasDisplayName()) {
                String name = shield.getItemMeta().getDisplayName();
                if (name.contains("Blossom Shield")) {
                    UUID uuid = victim.getUniqueId();
                    long now = System.currentTimeMillis();
                    long lastBlock = shieldCooldown.getOrDefault(uuid, 0L);

                    if (now - lastBlock > 10000) {
                        shieldCooldown.put(uuid, now);

                        double currentHealth = victim.getHealth();
                        double maxHealth = victim.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
                        victim.setHealth(Math.min(maxHealth, currentHealth + 2.0));

                        victim.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, victim.getLocation().add(0, 1, 0), 10, 0.3, 0.5, 0.3, 0.0);
                        victim.playSound(victim.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                        victim.sendMessage(ChatColor.LIGHT_PURPLE + "🌸 Blossom Shield: Restored 1 Heart!");
                    }
                }
            }
        }
    }

    private void startPassiveEffectsTracker() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : org.bukkit.Bukkit.getOnlinePlayers()) {
                    ItemStack hand = player.getInventory().getItemInMainHand();
                    if (hand != null && hand.hasItemMeta() && hand.getItemMeta().hasDisplayName()) {
                        String name = hand.getItemMeta().getDisplayName();
                        if (name.contains("Shadow Blade")) {
                            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 0, true, false, true));
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }
}
