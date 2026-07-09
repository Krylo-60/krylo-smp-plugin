package com.krylo.smp.commands;

import com.krylo.smp.KryloSMP;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

/**
 * Handles /tpa, /tpaccept, /tpdeny commands.
 * Teleport requests expire after the configured timeout.
 */
public class TpaCommand implements CommandExecutor {

    private final KryloSMP plugin;

    public TpaCommand(KryloSMP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command!");
            return true;
        }

        Map<UUID, UUID> requests = plugin.getTpaRequests();

        switch (cmd.getName().toLowerCase()) {
            case "tpa" -> {
                if (args.length < 1) {
                    player.sendMessage(ChatColor.RED + "Usage: /tpa <player>");
                    return true;
                }

                Player target = Bukkit.getPlayer(args[0]);
                if (target == null || !target.isOnline()) {
                    player.sendMessage(ChatColor.RED + "⚠ Player '" + args[0] + "' not found or offline.");
                    return true;
                }

                if (target.equals(player)) {
                    player.sendMessage(ChatColor.RED + "⚠ You can't teleport to yourself!");
                    return true;
                }

                requests.put(target.getUniqueId(), player.getUniqueId());
                player.sendMessage(ChatColor.GREEN + "✓ Teleport request sent to " + ChatColor.AQUA + target.getName() + ChatColor.GREEN + "!");
                target.sendMessage("");
                target.sendMessage(ChatColor.GOLD + "⚡ " + ChatColor.AQUA + player.getName() + ChatColor.YELLOW + " wants to teleport to you!");
                target.sendMessage(ChatColor.GREEN + "  /tpaccept" + ChatColor.GRAY + " to accept  |  " + ChatColor.RED + "/tpdeny" + ChatColor.GRAY + " to deny");
                target.sendMessage("");
                target.playSound(target.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);

                // Auto-expire the request after timeout
                int timeout = plugin.getConfig().getInt("tpa-timeout", 60);
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    UUID stored = requests.get(target.getUniqueId());
                    if (stored != null && stored.equals(player.getUniqueId())) {
                        requests.remove(target.getUniqueId());
                        if (player.isOnline()) {
                            player.sendMessage(ChatColor.GRAY + "⏱ Your teleport request to " + target.getName() + " has expired.");
                        }
                    }
                }, timeout * 20L); // Convert seconds to ticks
            }

            case "tpaccept" -> {
                UUID requesterId = requests.get(player.getUniqueId());
                if (requesterId == null) {
                    player.sendMessage(ChatColor.RED + "⚠ You have no pending teleport requests.");
                    return true;
                }

                Player requester = Bukkit.getPlayer(requesterId);
                requests.remove(player.getUniqueId());

                if (requester == null || !requester.isOnline()) {
                    player.sendMessage(ChatColor.RED + "⚠ The requesting player is no longer online.");
                    return true;
                }

                requester.teleport(player.getLocation());
                requester.sendMessage(ChatColor.AQUA + "⚡ " + ChatColor.WHITE + "Teleported to " + ChatColor.GREEN + player.getName() + ChatColor.WHITE + "!");
                player.sendMessage(ChatColor.GREEN + "✓ Teleport request from " + ChatColor.AQUA + requester.getName() + ChatColor.GREEN + " accepted!");
                requester.playSound(requester.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1.5f);
                player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1.5f);
            }

            case "tpdeny" -> {
                UUID denyId = requests.get(player.getUniqueId());
                if (denyId == null) {
                    player.sendMessage(ChatColor.RED + "⚠ You have no pending teleport requests.");
                    return true;
                }

                Player denied = Bukkit.getPlayer(denyId);
                requests.remove(player.getUniqueId());

                player.sendMessage(ChatColor.YELLOW + "✗ Teleport request denied.");
                if (denied != null && denied.isOnline()) {
                    denied.sendMessage(ChatColor.RED + "✗ " + player.getName() + " denied your teleport request.");
                }
            }
        }

        return true;
    }
}
