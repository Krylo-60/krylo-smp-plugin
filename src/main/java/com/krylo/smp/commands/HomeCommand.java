package com.krylo.smp.commands;

import com.krylo.smp.KryloSMP;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Handles /home, /sethome, /delhome commands.
 * Each player can save one personal home location.
 */
public class HomeCommand implements CommandExecutor {

    private final KryloSMP plugin;

    public HomeCommand(KryloSMP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command!");
            return true;
        }

        String uuid = player.getUniqueId().toString();
        String path = "homes." + uuid;

        switch (cmd.getName().toLowerCase()) {
            case "sethome" -> {
                Location loc = player.getLocation();
                plugin.getConfig().set(path + ".world", loc.getWorld().getName());
                plugin.getConfig().set(path + ".x", loc.getX());
                plugin.getConfig().set(path + ".y", loc.getY());
                plugin.getConfig().set(path + ".z", loc.getZ());
                plugin.getConfig().set(path + ".yaw", (double) loc.getYaw());
                plugin.getConfig().set(path + ".pitch", (double) loc.getPitch());
                plugin.saveConfig();

                player.sendMessage(ChatColor.GREEN + "✓ Home saved at your current location!");
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.2f);
            }

            case "home" -> {
                if (!plugin.getConfig().contains(path + ".world")) {
                    player.sendMessage(ChatColor.RED + "⚠ You haven't set a home yet! Use /sethome");
                    return true;
                }

                String worldName = plugin.getConfig().getString(path + ".world");
                double x = plugin.getConfig().getDouble(path + ".x");
                double y = plugin.getConfig().getDouble(path + ".y");
                double z = plugin.getConfig().getDouble(path + ".z");
                float yaw = (float) plugin.getConfig().getDouble(path + ".yaw");
                float pitch = (float) plugin.getConfig().getDouble(path + ".pitch");

                Location homeLoc = new Location(plugin.getServer().getWorld(worldName), x, y, z, yaw, pitch);
                player.teleport(homeLoc);
                player.sendMessage(ChatColor.AQUA + "🏠 " + ChatColor.WHITE + "Teleported home!");
                player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1.5f);
            }

            case "delhome" -> {
                if (!plugin.getConfig().contains(path + ".world")) {
                    player.sendMessage(ChatColor.RED + "⚠ You don't have a home set!");
                    return true;
                }

                plugin.getConfig().set(path, null);
                plugin.saveConfig();
                player.sendMessage(ChatColor.YELLOW + "✗ Home deleted.");
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
            }
        }

        return true;
    }
}
