package com.krylo.smp.commands;

import com.krylo.smp.KryloSMP;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

/**
 * Handles /customitem command to give legendary custom weapons.
 * Admin-only command.
 */
public class CustomItemCommand implements CommandExecutor {

    private final KryloSMP plugin;

    public CustomItemCommand(KryloSMP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command!");
            return true;
        }

        if (!player.hasPermission("krylosmp.admin")) {
            player.sendMessage(ChatColor.RED + "⚠ You don't have permission to use this command!");
            return true;
        }

        if (args.length < 1) {
            player.sendMessage("");
            player.sendMessage(ChatColor.GOLD + "  ⚡ Custom Items Available:");
            player.sendMessage(ChatColor.AQUA + "  /customitem thunderblade" + ChatColor.GRAY + " — Legendary lightning sword");
            player.sendMessage(ChatColor.AQUA + "  /customitem infernopick" + ChatColor.GRAY + " — Fire-infused pickaxe");
            player.sendMessage(ChatColor.AQUA + "  /customitem voidbow" + ChatColor.GRAY + " — Infinite power bow");
            player.sendMessage(ChatColor.AQUA + "  /customitem kryloaxe" + ChatColor.GRAY + " — The ultimate axe");
            player.sendMessage(ChatColor.AQUA + "  /customitem godarmor" + ChatColor.GRAY + " — Full set of god armor");
            player.sendMessage("");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "thunderblade" -> {
                ItemStack sword = new ItemStack(Material.NETHERITE_SWORD, 1);
                ItemMeta meta = sword.getItemMeta();
                meta.setDisplayName(ChatColor.GOLD + "⚡ " + ChatColor.AQUA + "Thunderblade of Krylo");
                meta.setLore(Arrays.asList(
                    "",
                    ChatColor.GRAY + "Forged in the depths of the Nether",
                    ChatColor.GRAY + "by the legendary smith " + ChatColor.GOLD + "Krishiv",
                    "",
                    ChatColor.DARK_PURPLE + "★ Legendary Weapon"
                ));
                meta.addEnchant(Enchantment.SHARPNESS, 10, true);
                meta.addEnchant(Enchantment.FIRE_ASPECT, 3, true);
                meta.addEnchant(Enchantment.KNOCKBACK, 3, true);
                meta.addEnchant(Enchantment.LOOTING, 5, true);
                meta.addEnchant(Enchantment.UNBREAKING, 10, true);
                meta.addEnchant(Enchantment.MENDING, 1, true);
                meta.setUnbreakable(true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                sword.setItemMeta(meta);
                player.getInventory().addItem(sword);
                player.sendMessage(ChatColor.GOLD + "⚡ " + ChatColor.WHITE + "You received the " + ChatColor.AQUA + "Thunderblade of Krylo" + ChatColor.WHITE + "!");
                player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
            }

            case "infernopick" -> {
                ItemStack pick = new ItemStack(Material.NETHERITE_PICKAXE, 1);
                ItemMeta meta = pick.getItemMeta();
                meta.setDisplayName(ChatColor.RED + "🔥 " + ChatColor.GOLD + "Inferno Pickaxe");
                meta.setLore(Arrays.asList(
                    "",
                    ChatColor.GRAY + "Auto-smelts everything it mines",
                    ChatColor.GRAY + "Blessed by " + ChatColor.GOLD + "Krishiv",
                    "",
                    ChatColor.DARK_PURPLE + "★ Legendary Tool"
                ));
                meta.addEnchant(Enchantment.EFFICIENCY, 10, true);
                meta.addEnchant(Enchantment.FORTUNE, 5, true);
                meta.addEnchant(Enchantment.UNBREAKING, 10, true);
                meta.addEnchant(Enchantment.MENDING, 1, true);
                meta.setUnbreakable(true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                pick.setItemMeta(meta);
                player.getInventory().addItem(pick);
                player.sendMessage(ChatColor.GOLD + "🔥 " + ChatColor.WHITE + "You received the " + ChatColor.GOLD + "Inferno Pickaxe" + ChatColor.WHITE + "!");
                player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
            }

            case "voidbow" -> {
                ItemStack bow = new ItemStack(Material.BOW, 1);
                ItemMeta meta = bow.getItemMeta();
                meta.setDisplayName(ChatColor.DARK_PURPLE + "🏹 " + ChatColor.LIGHT_PURPLE + "Void Bow");
                meta.setLore(Arrays.asList(
                    "",
                    ChatColor.GRAY + "Fires arrows through dimensions",
                    ChatColor.GRAY + "Engineered by " + ChatColor.GOLD + "Krishiv",
                    "",
                    ChatColor.DARK_PURPLE + "★ Legendary Weapon"
                ));
                meta.addEnchant(Enchantment.POWER, 10, true);
                meta.addEnchant(Enchantment.PUNCH, 5, true);
                meta.addEnchant(Enchantment.FLAME, 2, true);
                meta.addEnchant(Enchantment.INFINITY, 1, true);
                meta.addEnchant(Enchantment.UNBREAKING, 10, true);
                meta.setUnbreakable(true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                bow.setItemMeta(meta);
                player.getInventory().addItem(bow);
                player.sendMessage(ChatColor.DARK_PURPLE + "🏹 " + ChatColor.WHITE + "You received the " + ChatColor.LIGHT_PURPLE + "Void Bow" + ChatColor.WHITE + "!");
                player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
            }

            case "kryloaxe" -> {
                ItemStack axe = new ItemStack(Material.NETHERITE_AXE, 1);
                ItemMeta meta = axe.getItemMeta();
                meta.setDisplayName(ChatColor.GREEN + "🪓 " + ChatColor.DARK_GREEN + "Krylo's Executioner");
                meta.setLore(Arrays.asList(
                    "",
                    ChatColor.GRAY + "One swing to rule them all",
                    ChatColor.GRAY + "Crafted by " + ChatColor.GOLD + "Krishiv",
                    "",
                    ChatColor.DARK_PURPLE + "★ Legendary Weapon"
                ));
                meta.addEnchant(Enchantment.SHARPNESS, 10, true);
                meta.addEnchant(Enchantment.EFFICIENCY, 10, true);
                meta.addEnchant(Enchantment.UNBREAKING, 10, true);
                meta.addEnchant(Enchantment.MENDING, 1, true);
                meta.setUnbreakable(true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                axe.setItemMeta(meta);
                player.getInventory().addItem(axe);
                player.sendMessage(ChatColor.GREEN + "🪓 " + ChatColor.WHITE + "You received " + ChatColor.DARK_GREEN + "Krylo's Executioner" + ChatColor.WHITE + "!");
                player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
            }

            case "godarmor" -> {
                Material[] armorPieces = {Material.NETHERITE_HELMET, Material.NETHERITE_CHESTPLATE, Material.NETHERITE_LEGGINGS, Material.NETHERITE_BOOTS};
                String[] names = {"Crown of Krylo", "Chestplate of Krylo", "Leggings of Krylo", "Boots of Krylo"};
                String[] emojis = {"👑", "🛡️", "⚔️", "👢"};

                for (int i = 0; i < armorPieces.length; i++) {
                    ItemStack armor = new ItemStack(armorPieces[i], 1);
                    ItemMeta meta = armor.getItemMeta();
                    meta.setDisplayName(ChatColor.GOLD + emojis[i] + " " + ChatColor.AQUA + names[i]);
                    meta.setLore(Arrays.asList(
                        "",
                        ChatColor.GRAY + "Divine protection from " + ChatColor.GOLD + "Krishiv",
                        "",
                        ChatColor.DARK_PURPLE + "★ Legendary Armor"
                    ));
                    meta.addEnchant(Enchantment.PROTECTION, 10, true);
                    meta.addEnchant(Enchantment.UNBREAKING, 10, true);
                    meta.addEnchant(Enchantment.MENDING, 1, true);
                    meta.addEnchant(Enchantment.THORNS, 5, true);
                    meta.setUnbreakable(true);
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    armor.setItemMeta(meta);
                    player.getInventory().addItem(armor);
                }

                player.sendMessage(ChatColor.GOLD + "👑 " + ChatColor.WHITE + "You received the full " + ChatColor.AQUA + "God Armor Set" + ChatColor.WHITE + "!");
                player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 0.8f);
            }

            default -> {
                player.sendMessage(ChatColor.RED + "⚠ Unknown item '" + args[0] + "'. Use /customitem to see available items.");
            }
        }

        return true;
    }
}
