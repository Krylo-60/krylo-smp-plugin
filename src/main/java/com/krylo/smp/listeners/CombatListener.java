package com.krylo.smp.listeners;

import com.krylo.smp.KryloSMP;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

/**
 * Awards coins for player kills and applies a death penalty.
 */
public class CombatListener implements Listener {

    private final KryloSMP plugin;

    public CombatListener(KryloSMP plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        // Death penalty
        double deathPenalty = plugin.getConfig().getDouble("death-penalty", 10.0);
        if (deathPenalty > 0) {
            plugin.removeBalance(victim.getUniqueId(), deathPenalty);
            victim.sendMessage(ChatColor.RED + "💀 You lost " + ChatColor.GOLD + String.format("%.0f", deathPenalty) + " ⛃" + ChatColor.RED + " on death.");
        }

        // Kill reward
        if (killer != null && !killer.equals(victim)) {
            double killReward = plugin.getConfig().getDouble("kill-reward", 25.0);
            plugin.addBalance(killer.getUniqueId(), killReward);
            killer.sendMessage(ChatColor.GREEN + "⚔ Kill reward: +" + ChatColor.GOLD + String.format("%.0f", killReward) + " ⛃" + ChatColor.GREEN + " for eliminating " + ChatColor.AQUA + victim.getName());
        }
    }

    @EventHandler
    public void onEntityDamage(org.bukkit.event.entity.EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player damager)) return;
        if (!(event.getEntity() instanceof Player victim)) return;

        org.bukkit.inventory.ItemStack item = damager.getInventory().getItemInMainHand();
        
        // 1. Ownership security gate check for all custom items
        if (isThiefWielding(damager, item)) {
            event.setCancelled(true);
            return;
        }

        if (item == null || !item.hasItemMeta()) return;
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        String displayName = meta.getDisplayName();
        if (displayName.contains("THE BAN HAMMER")) {
            event.setCancelled(true);

            // Double check Krylo_MC check (handled in isThiefWielding, but let's keep it clean!)
            if (!damager.getName().equalsIgnoreCase("Krylo_MC")) {
                return;
            }

            String victimName = victim.getName();
            victim.getWorld().strikeLightning(victim.getLocation());

            // Broadcast message
            plugin.getServer().broadcastMessage("");
            plugin.getServer().broadcastMessage(ChatColor.DARK_RED + "⚡ " + ChatColor.RED + ChatColor.BOLD.toString() + "[BAN HAMMER] " + ChatColor.GOLD + "Krylo_MC" + ChatColor.WHITE + " has smited " + ChatColor.RED + victimName + ChatColor.WHITE + " out of existence!");
            plugin.getServer().broadcastMessage("");

            // Ban permanently
            plugin.getServer().getBannedPlayers().add(plugin.getServer().getOfflinePlayer(victim.getUniqueId()));
            
            // Kick instantly
            victim.kickPlayer(ChatColor.RED + ChatColor.BOLD.toString() + "⚡ THE BAN HAMMER HAS SPOKEN! ⚡\n\n" + ChatColor.WHITE + "You have been permanently banned by " + ChatColor.GOLD + "Krylo_MC" + ChatColor.WHITE + ".");
        }
    }

    @EventHandler
    public void onBlockBreak(org.bukkit.event.block.BlockBreakEvent event) {
        Player player = event.getPlayer();
        org.bukkit.inventory.ItemStack item = player.getInventory().getItemInMainHand();
        if (isThiefWielding(player, item)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBowShoot(org.bukkit.event.entity.EntityShootBowEvent event) {
        if (event.getEntity() instanceof Player player) {
            org.bukkit.inventory.ItemStack item = event.getBow();
            if (isThiefWielding(player, item)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(org.bukkit.event.player.PlayerInteractEvent event) {
        Player player = event.getPlayer();
        org.bukkit.inventory.ItemStack item = event.getItem();
        if (isThiefWielding(player, item)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(org.bukkit.event.inventory.InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        
        org.bukkit.inventory.ItemStack item = event.getCurrentItem();
        if (item == null || !item.hasItemMeta()) return;
        
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasLore()) return;
        
        for (String line : meta.getLore()) {
            if (line.contains("Krishiv") || line.contains("Krylo_MC")) {
                if (!player.getName().equalsIgnoreCase("Krylo_MC")) {
                    if (event.getSlotType() == org.bukkit.event.inventory.InventoryType.SlotType.ARMOR || event.isShiftClick()) {
                        event.setCancelled(true);
                        player.getWorld().strikeLightning(player.getLocation());
                        player.sendMessage(ChatColor.RED + "⚡ You cannot equip Krylo_MC's divine armor!");
                    }
                }
            }
        }
    }

    /**
     * Checks if the item is custom and if the wielder is a thief (not Krylo_MC).
     * If so, strikes them with lightning and drops the item on the ground.
     */
    private boolean isThiefWielding(Player player, org.bukkit.inventory.ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasLore()) return false;
        
        for (String line : meta.getLore()) {
            if (line.contains("Krishiv") || line.contains("Krylo_MC")) {
                if (!player.getName().equalsIgnoreCase("Krylo_MC")) {
                    player.getWorld().strikeLightning(player.getLocation());
                    player.sendMessage(ChatColor.RED + "⚡ This is a divine weapon of Krylo_MC! You cannot wield it!");
                    
                    // Remove from hand and drop naturally
                    player.getInventory().setItemInMainHand(null);
                    player.getWorld().dropItemNaturally(player.getLocation(), item);
                    return true;
                }
            }
        }
        return false;
    }
}
