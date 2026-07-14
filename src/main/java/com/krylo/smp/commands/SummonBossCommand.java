package com.krylo.smp.commands;

import com.krylo.smp.boss.CustomBossManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SummonBossCommand implements CommandExecutor {

    private final CustomBossManager bossManager;

    public SummonBossCommand(CustomBossManager bossManager) {
        this.bossManager = bossManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can run this command!");
            return true;
        }

        Player player = (Player) sender;
        if (!player.isOp()) {
            player.sendMessage(ChatColor.RED + "You do not have permission to run this command!");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Usage: /summonboss <warlord|elder>");
            return true;
        }

        String bossType = args[0].toLowerCase();
        if ("warlord".equals(bossType)) {
            bossManager.spawnWarlord(player.getLocation());
            player.sendMessage(ChatColor.GREEN + "Successfully spawned Warlord of Shadows!");
            return true;
        } else if ("elder".equals(bossType)) {
            bossManager.spawnCherryElder(player.getLocation());
            player.sendMessage(ChatColor.GREEN + "Successfully spawned Cherry Elder!");
            return true;
        } else {
            player.sendMessage(ChatColor.RED + "Unknown boss type! Try: /summonboss <warlord|elder>");
            return true;
        }
    }
}
