package com.krylo.smp.listeners;

import com.krylo.smp.KryloSMP;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

/**
 * Formats chat messages with economy balance display.
 */
public class ChatListener implements Listener {

    private final KryloSMP plugin;

    public ChatListener(KryloSMP plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        double balance = plugin.getBalance(player.getUniqueId());

        String format = plugin.getConfig().getString("chat.format",
            "&8[&a${balance}⛃&8] &7{player} &f» &r{message}");

        format = format
            .replace("{player}", player.getName())
            .replace("{message}", event.getMessage())
            .replace("${balance}", String.format("%.0f", balance));

        event.setFormat(colorize(format));
    }

    private String colorize(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
