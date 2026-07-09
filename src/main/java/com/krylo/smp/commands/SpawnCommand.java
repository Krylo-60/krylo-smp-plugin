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
 * Handles /spawn and /setspawn commands.
 */
public class SpawnCommand implements CommandExecutor {

    private final KryloSMP plugin;

    public SpawnCommand(KryloSMP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command!");
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("setspawn")) {
            if (!player.hasPermission("krylosmp.admin")) {
                player.sendMessage(ChatColor.RED + "⚠ You don't have permission to set spawn!");
                return true;
            }

            Location loc = player.getLocation();
            plugin.getConfig().set("spawn.world", loc.getWorld().getName());
            plugin.getConfig().set("spawn.x", loc.getX());
            plugin.getConfig().set("spawn.y", loc.getY());
            plugin.getConfig().set("spawn.z", loc.getZ());
            plugin.getConfig().set("spawn.yaw", (double) loc.getYaw());
            plugin.getConfig().set("spawn.pitch", (double) loc.getPitch());
            plugin.saveConfig();

            player.sendMessage(ChatColor.GREEN + "✓ Server spawn set to your current location!");
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.2f);
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("spawn")) {
            if (!plugin.getConfig().contains("spawn.world")) {
                player.sendMessage(ChatColor.RED + "⚠ Spawn hasn't been set yet! Ask an admin to use /setspawn.");
                return true;
            }

            String worldName = plugin.getConfig().getString("spawn.world");
            double x = plugin.getConfig().getDouble("spawn.x");
            double y = plugin.getConfig().getDouble("spawn.y");
            double z = plugin.getConfig().getDouble("spawn.z");
            float yaw = (float) plugin.getConfig().getDouble("spawn.yaw");
            float pitch = (float) plugin.getConfig().getDouble("spawn.pitch");

            Location spawnLoc = new Location(plugin.getServer().getWorld(worldName), x, y, z, yaw, pitch);
            player.teleport(spawnLoc);
            player.sendMessage(ChatColor.AQUA + "⚡ " + ChatColor.WHITE + "Teleported to spawn!");
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1.5f);
            return true;
        }

        return false;
    }
}
