package com.krylo.smp.commands;

import com.krylo.smp.KryloSMP;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Handles /balance, /pay, and /shop commands.
 * Includes a GUI shop with configurable item prices.
 */
public class EconomyCommand implements CommandExecutor, Listener {

    private final KryloSMP plugin;
    private static final String SHOP_TITLE = ChatColor.GOLD + "⚡ KryloSMP Shop";

    public EconomyCommand(KryloSMP plugin) {
        this.plugin = plugin;
        // Register this as a listener for shop GUI clicks
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command!");
            return true;
        }

        switch (cmd.getName().toLowerCase()) {
            case "balance", "bal", "money" -> {
                if (args.length > 0) {
                    Player target = Bukkit.getPlayer(args[0]);
                    if (target != null) {
                        double bal = plugin.getBalance(target.getUniqueId());
                        player.sendMessage(ChatColor.AQUA + target.getName() + "'s Balance: " + ChatColor.GOLD + String.format("%.0f", bal) + " ⛃");
                    } else {
                        player.sendMessage(ChatColor.RED + "⚠ Player not found.");
                    }
                } else {
                    double bal = plugin.getBalance(player.getUniqueId());
                    player.sendMessage(ChatColor.GREEN + "💰 Your Balance: " + ChatColor.GOLD + String.format("%.0f", bal) + " ⛃");
                }
            }

            case "pay" -> {
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /pay <player> <amount>");
                    return true;
                }

                Player target = Bukkit.getPlayer(args[0]);
                if (target == null || !target.isOnline()) {
                    player.sendMessage(ChatColor.RED + "⚠ Player not found or offline.");
                    return true;
                }

                if (target.equals(player)) {
                    player.sendMessage(ChatColor.RED + "⚠ You can't pay yourself!");
                    return true;
                }

                double amount;
                try {
                    amount = Double.parseDouble(args[1]);
                    if (amount <= 0) throw new NumberFormatException();
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "⚠ Invalid amount. Must be a positive number.");
                    return true;
                }

                if (plugin.removeBalance(player.getUniqueId(), amount)) {
                    plugin.addBalance(target.getUniqueId(), amount);
                    player.sendMessage(ChatColor.GREEN + "✓ Sent " + ChatColor.GOLD + String.format("%.0f", amount) + " ⛃" + ChatColor.GREEN + " to " + ChatColor.AQUA + target.getName());
                    target.sendMessage(ChatColor.GREEN + "💰 " + ChatColor.AQUA + player.getName() + ChatColor.GREEN + " sent you " + ChatColor.GOLD + String.format("%.0f", amount) + " ⛃");
                    target.playSound(target.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.5f);
                } else {
                    player.sendMessage(ChatColor.RED + "⚠ Insufficient funds! You have " + ChatColor.GOLD + String.format("%.0f", plugin.getBalance(player.getUniqueId())) + " ⛃");
                }
            }

            case "shop" -> {
                openShopGUI(player);
            }
        }

        return true;
    }

    private void openShopGUI(Player player) {
        Map<String, Integer> shopItems = getShopItems();
        int size = Math.min(54, ((shopItems.size() / 9) + 1) * 9); // Round up to nearest 9
        if (size < 9) size = 9;

        Inventory shopInv = Bukkit.createInventory(null, size, SHOP_TITLE);

        int slot = 0;
        for (Map.Entry<String, Integer> entry : shopItems.entrySet()) {
            try {
                Material mat = Material.valueOf(entry.getKey().toUpperCase());
                int price = entry.getValue();

                ItemStack item = new ItemStack(mat, 1);
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(ChatColor.AQUA + formatMaterialName(mat.name()));
                    meta.setLore(Arrays.asList(
                        "",
                        ChatColor.GRAY + "Price: " + ChatColor.GOLD + price + " ⛃",
                        "",
                        ChatColor.YELLOW + "Click to buy!"
                    ));
                    item.setItemMeta(meta);
                }
                shopInv.setItem(slot++, item);
            } catch (IllegalArgumentException ignored) {}
        }

        player.openInventory(shopInv);
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1f, 1.2f);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getView().getTitle().equals(SHOP_TITLE)) {
            event.setCancelled(true);

            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return;

            Map<String, Integer> shopItems = getShopItems();
            String matKey = clicked.getType().name().toLowerCase();
            Integer price = shopItems.get(matKey);

            if (price == null) return;

            if (plugin.removeBalance(player.getUniqueId(), price)) {
                player.getInventory().addItem(new ItemStack(clicked.getType(), 1));
                player.sendMessage(ChatColor.GREEN + "✓ Purchased " + ChatColor.AQUA + formatMaterialName(clicked.getType().name()) + ChatColor.GREEN + " for " + ChatColor.GOLD + price + " ⛃");
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_YES, 1f, 1f);
            } else {
                player.sendMessage(ChatColor.RED + "⚠ Not enough coins! You need " + ChatColor.GOLD + price + " ⛃" + ChatColor.RED + " but have " + ChatColor.GOLD + String.format("%.0f", plugin.getBalance(player.getUniqueId())) + " ⛃");
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            }
        }
    }

    private Map<String, Integer> getShopItems() {
        Map<String, Integer> items = new LinkedHashMap<>();
        if (plugin.getConfig().isConfigurationSection("shop")) {
            for (String key : plugin.getConfig().getConfigurationSection("shop").getKeys(false)) {
                items.put(key, plugin.getConfig().getInt("shop." + key));
            }
        }
        return items;
    }

    private String formatMaterialName(String name) {
        String[] parts = name.toLowerCase().split("_");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (!p.isEmpty()) {
                sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1)).append(" ");
            }
        }
        return sb.toString().trim();
    }
}
