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
}
