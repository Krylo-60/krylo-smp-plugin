package com.krylo.smp.commands;

import com.krylo.smp.KryloSMP;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class LinkCommand implements CommandExecutor {

    private final KryloSMP plugin;

    public LinkCommand(KryloSMP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        sender.sendMessage("");
        sender.sendMessage(ChatColor.GOLD + "⚡ " + ChatColor.BOLD + "KryloSMP Discord Link");
        sender.sendMessage(ChatColor.GRAY + "To link your account, follow these steps:");
        sender.sendMessage(ChatColor.GRAY + "1. Go to our Discord server.");
        sender.sendMessage(ChatColor.GRAY + "2. Click the " + ChatColor.AQUA + "Link Account" + ChatColor.GRAY + " button.");
        sender.sendMessage(ChatColor.GRAY + "3. Enter your Minecraft username when prompted.");
        sender.sendMessage(ChatColor.GRAY + "4. Join this server to receive your link code!");
        sender.sendMessage("");
        return true;
    }
}
