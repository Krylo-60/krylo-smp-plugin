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
            player.sendMessage(ChatColor.GOLD + "  ⚡ Custom Items Available:");
            player.sendMessage(ChatColor.AQUA + "  /customitem thunderblade" + ChatColor.GRAY + " — Legendary lightning sword");
            player.sendMessage(ChatColor.AQUA + "  /customitem infernopick" + ChatColor.GRAY + " — Fire-infused pickaxe");
            player.sendMessage(ChatColor.AQUA + "  /customitem voidbow" + ChatColor.GRAY + " — Infinite power bow");
            player.sendMessage(ChatColor.AQUA + "  /customitem kryloaxe" + ChatColor.GRAY + " — The ultimate axe");
            player.sendMessage(ChatColor.AQUA + "  /customitem godarmor" + ChatColor.GRAY + " — Full set of god armor");
            player.sendMessage(ChatColor.AQUA + "  /customitem godmace" + ChatColor.GRAY + " — Overpowered heavy mace");
            player.sendMessage(ChatColor.AQUA + "  /customitem godspear" + ChatColor.GRAY + " — Thunder-summoning spear");
            player.sendMessage(ChatColor.AQUA + "  /customitem godshovel" + ChatColor.GRAY + " — Mountains-excavating shovel");
            player.sendMessage(ChatColor.AQUA + "  /customitem godhoe" + ChatColor.GRAY + " — Crops-harvesting scythe");
            player.sendMessage(ChatColor.AQUA + "  /customitem godshield" + ChatColor.GRAY + " — Attacks-blocking Aegis shield");
            player.sendMessage(ChatColor.AQUA + "  /customitem godcrossbow" + ChatColor.GRAY + " — Multishot rapid crossbow");
            player.sendMessage(ChatColor.AQUA + "  /customitem godelytra" + ChatColor.GRAY + " — Infinite flight Elytra wings");
            player.sendMessage(ChatColor.AQUA + "  /customitem infinity" + ChatColor.GRAY + " — Receive all God Gears at once!");
            player.sendMessage(ChatColor.AQUA + "  /customitem banhammer" + ChatColor.GRAY + " — Divine Ban Hammer (Krylo only)");
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

            case "godmace" -> {
                ItemStack mace = new ItemStack(Material.MACE, 1);
                ItemMeta meta = mace.getItemMeta();
                meta.setDisplayName(ChatColor.DARK_RED + "🔨 " + ChatColor.GOLD + "Godly Mace of Krylo");
                meta.setLore(Arrays.asList(
                    "",
                    ChatColor.GRAY + "Smashes enemies from above",
                    ChatColor.GRAY + "Forged by the divine " + ChatColor.GOLD + "Krishiv",
                    "",
                    ChatColor.DARK_PURPLE + "★ Legendary Weapon"
                ));
                meta.addEnchant(Enchantment.SHARPNESS, 10, true);
                meta.addEnchant(Enchantment.FIRE_ASPECT, 3, true);
                meta.addEnchant(Enchantment.KNOCKBACK, 3, true);
                meta.addEnchant(Enchantment.UNBREAKING, 10, true);
                meta.addEnchant(Enchantment.MENDING, 1, true);
                meta.setUnbreakable(true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                mace.setItemMeta(meta);
                player.getInventory().addItem(mace);
                player.sendMessage(ChatColor.GOLD + "🔨 " + ChatColor.WHITE + "You received the " + ChatColor.GOLD + "Godly Mace of Krylo" + ChatColor.WHITE + "!");
                player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
            }

            case "godspear" -> {
                ItemStack spear = new ItemStack(Material.TRIDENT, 1);
                ItemMeta meta = spear.getItemMeta();
                meta.setDisplayName(ChatColor.AQUA + "🔱 " + ChatColor.BLUE + "Neptune's God Spear");
                meta.setLore(Arrays.asList(
                    "",
                    ChatColor.GRAY + "Throws lightning bolts at enemies",
                    ChatColor.GRAY + "Blessed by " + ChatColor.GOLD + "Krishiv",
                    "",
                    ChatColor.DARK_PURPLE + "★ Legendary Weapon"
                ));
                meta.addEnchant(Enchantment.IMPALING, 10, true);
                meta.addEnchant(Enchantment.LOYALTY, 5, true);
                meta.addEnchant(Enchantment.CHANNELING, 1, true);
                meta.addEnchant(Enchantment.UNBREAKING, 10, true);
                meta.addEnchant(Enchantment.MENDING, 1, true);
                meta.setUnbreakable(true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                spear.setItemMeta(meta);
                player.getInventory().addItem(spear);
                player.sendMessage(ChatColor.AQUA + "🔱 " + ChatColor.WHITE + "You received " + ChatColor.BLUE + "Neptune's God Spear" + ChatColor.WHITE + "!");
                player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
            }

            case "godshovel" -> {
                ItemStack shovel = new ItemStack(Material.NETHERITE_SHOVEL, 1);
                ItemMeta meta = shovel.getItemMeta();
                meta.setDisplayName(ChatColor.GOLD + "⛏️ " + ChatColor.YELLOW + "God's Excavator");
                meta.setLore(Arrays.asList(
                    "",
                    ChatColor.GRAY + "Digs entire mountains in seconds",
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
                shovel.setItemMeta(meta);
                player.getInventory().addItem(shovel);
                player.sendMessage(ChatColor.GOLD + "⛏️ " + ChatColor.WHITE + "You received " + ChatColor.YELLOW + "God's Excavator" + ChatColor.WHITE + "!");
                player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
            }

            case "godhoe" -> {
                ItemStack hoe = new ItemStack(Material.NETHERITE_HOE, 1);
                ItemMeta meta = hoe.getItemMeta();
                meta.setDisplayName(ChatColor.GREEN + "🌾 " + ChatColor.GOLD + "Scythe of Demeter");
                meta.setLore(Arrays.asList(
                    "",
                    ChatColor.GRAY + "Harvests entire fields in one swing",
                    ChatColor.GRAY + "Blessed by " + ChatColor.GOLD + "Krishiv",
                    "",
                    ChatColor.DARK_PURPLE + "★ Legendary Tool"
                ));
                meta.addEnchant(Enchantment.EFFICIENCY, 10, true);
                meta.addEnchant(Enchantment.UNBREAKING, 10, true);
                meta.addEnchant(Enchantment.MENDING, 1, true);
                meta.setUnbreakable(true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                hoe.setItemMeta(meta);
                player.getInventory().addItem(hoe);
                player.sendMessage(ChatColor.GREEN + "🌾 " + ChatColor.WHITE + "You received the " + ChatColor.GOLD + "Scythe of Demeter" + ChatColor.WHITE + "!");
                player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
            }

            case "godshield" -> {
                ItemStack shield = new ItemStack(Material.SHIELD, 1);
                ItemMeta meta = shield.getItemMeta();
                meta.setDisplayName(ChatColor.GOLD + "🛡️ " + ChatColor.AQUA + "Aegis Shield of Krylo");
                meta.setLore(Arrays.asList(
                    "",
                    ChatColor.GRAY + "Blocks attacks from even gods",
                    ChatColor.GRAY + "Blessed by " + ChatColor.GOLD + "Krishiv",
                    "",
                    ChatColor.DARK_PURPLE + "★ Legendary Armor"
                ));
                meta.addEnchant(Enchantment.UNBREAKING, 10, true);
                meta.addEnchant(Enchantment.MENDING, 1, true);
                meta.setUnbreakable(true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                shield.setItemMeta(meta);
                player.getInventory().addItem(shield);
                player.sendMessage(ChatColor.GOLD + "🛡️ " + ChatColor.WHITE + "You received the " + ChatColor.AQUA + "Aegis Shield of Krylo" + ChatColor.WHITE + "!");
                player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
            }

            case "godcrossbow" -> {
                ItemStack crossbow = new ItemStack(Material.CROSSBOW, 1);
                ItemMeta meta = crossbow.getItemMeta();
                meta.setDisplayName(ChatColor.GOLD + "🏹 " + ChatColor.RED + "God's Ballista");
                meta.setLore(Arrays.asList(
                    "",
                    ChatColor.GRAY + "Fires a spread of lethal projectiles",
                    ChatColor.GRAY + "Blessed by " + ChatColor.GOLD + "Krishiv",
                    "",
                    ChatColor.DARK_PURPLE + "★ Legendary Weapon"
                ));
                meta.addEnchant(Enchantment.MULTISHOT, 1, true);
                meta.addEnchant(Enchantment.QUICK_CHARGE, 5, true);
                meta.addEnchant(Enchantment.PIERCING, 4, true);
                meta.addEnchant(Enchantment.UNBREAKING, 10, true);
                meta.addEnchant(Enchantment.MENDING, 1, true);
                meta.setUnbreakable(true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                crossbow.setItemMeta(meta);
                player.getInventory().addItem(crossbow);
                player.sendMessage(ChatColor.GOLD + "🏹 " + ChatColor.WHITE + "You received " + ChatColor.RED + "God's Ballista" + ChatColor.WHITE + "!");
                player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
            }

            case "godelytra" -> {
                ItemStack elytra = new ItemStack(Material.ELYTRA, 1);
                ItemMeta meta = elytra.getItemMeta();
                meta.setDisplayName(ChatColor.GOLD + "✨ " + ChatColor.LIGHT_PURPLE + "Wings of Icarus");
                meta.setLore(Arrays.asList(
                    "",
                    ChatColor.GRAY + "Fly forever with infinite durability",
                    ChatColor.GRAY + "Blessed by " + ChatColor.GOLD + "Krishiv",
                    "",
                    ChatColor.DARK_PURPLE + "★ Legendary Armor"
                ));
                meta.addEnchant(Enchantment.UNBREAKING, 10, true);
                meta.addEnchant(Enchantment.MENDING, 1, true);
                meta.setUnbreakable(true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                elytra.setItemMeta(meta);
                player.getInventory().addItem(elytra);
                player.sendMessage(ChatColor.GOLD + "✨ " + ChatColor.WHITE + "You received the " + ChatColor.LIGHT_PURPLE + "Wings of Icarus" + ChatColor.WHITE + "!");
                player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
            }

            case "infinity", "godgears", "all" -> {
                // 1. Sword
                ItemStack sword = new ItemStack(Material.NETHERITE_SWORD, 1);
                ItemMeta swordMeta = sword.getItemMeta();
                swordMeta.setDisplayName(ChatColor.GOLD + "⚡ " + ChatColor.AQUA + "Thunderblade of Krylo");
                swordMeta.setLore(Arrays.asList("", ChatColor.GRAY + "Forged in the depths of the Nether", ChatColor.GRAY + "by the legendary smith " + ChatColor.GOLD + "Krishiv", "", ChatColor.DARK_PURPLE + "★ Legendary Weapon"));
                swordMeta.addEnchant(Enchantment.SHARPNESS, 10, true);
                swordMeta.addEnchant(Enchantment.FIRE_ASPECT, 3, true);
                swordMeta.addEnchant(Enchantment.KNOCKBACK, 3, true);
                swordMeta.addEnchant(Enchantment.LOOTING, 5, true);
                swordMeta.addEnchant(Enchantment.UNBREAKING, 10, true);
                swordMeta.addEnchant(Enchantment.MENDING, 1, true);
                swordMeta.setUnbreakable(true);
                swordMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                sword.setItemMeta(swordMeta);
                player.getInventory().addItem(sword);

                // 2. Pickaxe
                ItemStack pick = new ItemStack(Material.NETHERITE_PICKAXE, 1);
                ItemMeta pickMeta = pick.getItemMeta();
                pickMeta.setDisplayName(ChatColor.RED + "🔥 " + ChatColor.GOLD + "Inferno Pickaxe");
                pickMeta.setLore(Arrays.asList("", ChatColor.GRAY + "Auto-smelts everything it mines", ChatColor.GRAY + "Blessed by " + ChatColor.GOLD + "Krishiv", "", ChatColor.DARK_PURPLE + "★ Legendary Tool"));
                pickMeta.addEnchant(Enchantment.EFFICIENCY, 10, true);
                pickMeta.addEnchant(Enchantment.FORTUNE, 5, true);
                pickMeta.addEnchant(Enchantment.UNBREAKING, 10, true);
                pickMeta.addEnchant(Enchantment.MENDING, 1, true);
                pickMeta.setUnbreakable(true);
                pickMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                pick.setItemMeta(pickMeta);
                player.getInventory().addItem(pick);

                // 3. Bow
                ItemStack bow = new ItemStack(Material.BOW, 1);
                ItemMeta bowMeta = bow.getItemMeta();
                bowMeta.setDisplayName(ChatColor.DARK_PURPLE + "🏹 " + ChatColor.LIGHT_PURPLE + "Void Bow");
                bowMeta.setLore(Arrays.asList("", ChatColor.GRAY + "Fires arrows through dimensions", ChatColor.GRAY + "Engineered by " + ChatColor.GOLD + "Krishiv", "", ChatColor.DARK_PURPLE + "★ Legendary Weapon"));
                bowMeta.addEnchant(Enchantment.POWER, 10, true);
                bowMeta.addEnchant(Enchantment.PUNCH, 5, true);
                bowMeta.addEnchant(Enchantment.FLAME, 2, true);
                bowMeta.addEnchant(Enchantment.INFINITY, 1, true);
                bowMeta.addEnchant(Enchantment.UNBREAKING, 10, true);
                bowMeta.setUnbreakable(true);
                bowMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                bow.setItemMeta(bowMeta);
                player.getInventory().addItem(bow);

                // 4. Axe
                ItemStack axe = new ItemStack(Material.NETHERITE_AXE, 1);
                ItemMeta axeMeta = axe.getItemMeta();
                axeMeta.setDisplayName(ChatColor.GREEN + "🪓 " + ChatColor.DARK_GREEN + "Krylo's Executioner");
                axeMeta.setLore(Arrays.asList("", ChatColor.GRAY + "One swing to rule them all", ChatColor.GRAY + "Crafted by " + ChatColor.GOLD + "Krishiv", "", ChatColor.DARK_PURPLE + "★ Legendary Weapon"));
                axeMeta.addEnchant(Enchantment.SHARPNESS, 10, true);
                axeMeta.addEnchant(Enchantment.EFFICIENCY, 10, true);
                axeMeta.addEnchant(Enchantment.UNBREAKING, 10, true);
                axeMeta.addEnchant(Enchantment.MENDING, 1, true);
                axeMeta.setUnbreakable(true);
                axeMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                axe.setItemMeta(axeMeta);
                player.getInventory().addItem(axe);

                // 5. Mace
                ItemStack mace = new ItemStack(Material.MACE, 1);
                ItemMeta maceMeta = mace.getItemMeta();
                maceMeta.setDisplayName(ChatColor.DARK_RED + "🔨 " + ChatColor.GOLD + "Godly Mace of Krylo");
                maceMeta.setLore(Arrays.asList("", ChatColor.GRAY + "Smashes enemies from above", ChatColor.GRAY + "Forged by the divine " + ChatColor.GOLD + "Krishiv", "", ChatColor.DARK_PURPLE + "★ Legendary Weapon"));
                maceMeta.addEnchant(Enchantment.SHARPNESS, 10, true);
                maceMeta.addEnchant(Enchantment.FIRE_ASPECT, 3, true);
                maceMeta.addEnchant(Enchantment.KNOCKBACK, 3, true);
                maceMeta.addEnchant(Enchantment.UNBREAKING, 10, true);
                maceMeta.addEnchant(Enchantment.MENDING, 1, true);
                maceMeta.setUnbreakable(true);
                maceMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                mace.setItemMeta(maceMeta);
                player.getInventory().addItem(mace);

                // 6. Spear
                ItemStack spear = new ItemStack(Material.TRIDENT, 1);
                ItemMeta spearMeta = spear.getItemMeta();
                spearMeta.setDisplayName(ChatColor.AQUA + "🔱 " + ChatColor.BLUE + "Neptune's God Spear");
                spearMeta.setLore(Arrays.asList("", ChatColor.GRAY + "Throws lightning bolts at enemies", ChatColor.GRAY + "Blessed by " + ChatColor.GOLD + "Krishiv", "", ChatColor.DARK_PURPLE + "★ Legendary Weapon"));
                spearMeta.addEnchant(Enchantment.IMPALING, 10, true);
                spearMeta.addEnchant(Enchantment.LOYALTY, 5, true);
                spearMeta.addEnchant(Enchantment.CHANNELING, 1, true);
                spearMeta.addEnchant(Enchantment.UNBREAKING, 10, true);
                spearMeta.addEnchant(Enchantment.MENDING, 1, true);
                spearMeta.setUnbreakable(true);
                spearMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                spear.setItemMeta(spearMeta);
                player.getInventory().addItem(spear);

                // 7. Shovel
                ItemStack shovel = new ItemStack(Material.NETHERITE_SHOVEL, 1);
                ItemMeta shovelMeta = shovel.getItemMeta();
                shovelMeta.setDisplayName(ChatColor.GOLD + "⛏️ " + ChatColor.YELLOW + "God's Excavator");
                shovelMeta.setLore(Arrays.asList("", ChatColor.GRAY + "Digs entire mountains in seconds", ChatColor.GRAY + "Blessed by " + ChatColor.GOLD + "Krishiv", "", ChatColor.DARK_PURPLE + "★ Legendary Tool"));
                shovelMeta.addEnchant(Enchantment.EFFICIENCY, 10, true);
                shovelMeta.addEnchant(Enchantment.FORTUNE, 5, true);
                shovelMeta.addEnchant(Enchantment.UNBREAKING, 10, true);
                shovelMeta.addEnchant(Enchantment.MENDING, 1, true);
                shovelMeta.setUnbreakable(true);
                shovelMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                shovel.setItemMeta(shovelMeta);
                player.getInventory().addItem(shovel);

                // 8. Hoe
                ItemStack hoe = new ItemStack(Material.NETHERITE_HOE, 1);
                ItemMeta hoeMeta = hoe.getItemMeta();
                hoeMeta.setDisplayName(ChatColor.GREEN + "🌾 " + ChatColor.GOLD + "Scythe of Demeter");
                hoeMeta.setLore(Arrays.asList("", ChatColor.GRAY + "Harvests entire fields in one swing", ChatColor.GRAY + "Blessed by " + ChatColor.GOLD + "Krishiv", "", ChatColor.DARK_PURPLE + "★ Legendary Tool"));
                hoeMeta.addEnchant(Enchantment.EFFICIENCY, 10, true);
                hoeMeta.addEnchant(Enchantment.UNBREAKING, 10, true);
                hoeMeta.addEnchant(Enchantment.MENDING, 1, true);
                hoeMeta.setUnbreakable(true);
                hoeMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                hoe.setItemMeta(hoeMeta);
                player.getInventory().addItem(hoe);

                // 9. Shield
                ItemStack shield = new ItemStack(Material.SHIELD, 1);
                ItemMeta shieldMeta = shield.getItemMeta();
                shieldMeta.setDisplayName(ChatColor.GOLD + "🛡️ " + ChatColor.AQUA + "Aegis Shield of Krylo");
                shieldMeta.setLore(Arrays.asList("", ChatColor.GRAY + "Blocks attacks from even gods", ChatColor.GRAY + "Blessed by " + ChatColor.GOLD + "Krishiv", "", ChatColor.DARK_PURPLE + "★ Legendary Armor"));
                shieldMeta.addEnchant(Enchantment.UNBREAKING, 10, true);
                shieldMeta.addEnchant(Enchantment.MENDING, 1, true);
                shieldMeta.setUnbreakable(true);
                shieldMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                shield.setItemMeta(shieldMeta);
                player.getInventory().addItem(shield);

                // 10. Crossbow
                ItemStack crossbow = new ItemStack(Material.CROSSBOW, 1);
                ItemMeta crossbowMeta = crossbow.getItemMeta();
                crossbowMeta.setDisplayName(ChatColor.GOLD + "🏹 " + ChatColor.RED + "God's Ballista");
                crossbowMeta.setLore(Arrays.asList("", ChatColor.GRAY + "Fires a spread of lethal projectiles", ChatColor.GRAY + "Blessed by " + ChatColor.GOLD + "Krishiv", "", ChatColor.DARK_PURPLE + "★ Legendary Weapon"));
                crossbowMeta.addEnchant(Enchantment.MULTISHOT, 1, true);
                crossbowMeta.addEnchant(Enchantment.QUICK_CHARGE, 5, true);
                crossbowMeta.addEnchant(Enchantment.PIERCING, 4, true);
                crossbowMeta.addEnchant(Enchantment.UNBREAKING, 10, true);
                crossbowMeta.addEnchant(Enchantment.MENDING, 1, true);
                crossbowMeta.setUnbreakable(true);
                crossbowMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                crossbow.setItemMeta(crossbowMeta);
                player.getInventory().addItem(crossbow);

                // 11. Elytra
                ItemStack elytra = new ItemStack(Material.ELYTRA, 1);
                ItemMeta elytraMeta = elytra.getItemMeta();
                elytraMeta.setDisplayName(ChatColor.GOLD + "✨ " + ChatColor.LIGHT_PURPLE + "Wings of Icarus");
                elytraMeta.setLore(Arrays.asList("", ChatColor.GRAY + "Fly forever with infinite durability", ChatColor.GRAY + "Blessed by " + ChatColor.GOLD + "Krishiv", "", ChatColor.DARK_PURPLE + "★ Legendary Armor"));
                elytraMeta.addEnchant(Enchantment.UNBREAKING, 10, true);
                elytraMeta.addEnchant(Enchantment.MENDING, 1, true);
                elytraMeta.setUnbreakable(true);
                elytraMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                elytra.setItemMeta(elytraMeta);
                player.getInventory().addItem(elytra);

                // 12. Armor Set
                Material[] armorPieces = {Material.NETHERITE_HELMET, Material.NETHERITE_CHESTPLATE, Material.NETHERITE_LEGGINGS, Material.NETHERITE_BOOTS};
                String[] names = {"Crown of Krylo", "Chestplate of Krylo", "Leggings of Krylo", "Boots of Krylo"};
                String[] emojis = {"👑", "🛡️", "⚔️", "👢"};

                for (int i = 0; i < armorPieces.length; i++) {
                    ItemStack armor = new ItemStack(armorPieces[i], 1);
                    ItemMeta armorMeta = armor.getItemMeta();
                    armorMeta.setDisplayName(ChatColor.GOLD + emojis[i] + " " + ChatColor.AQUA + names[i]);
                    armorMeta.setLore(Arrays.asList("", ChatColor.GRAY + "Divine protection from " + ChatColor.GOLD + "Krishiv", "", ChatColor.DARK_PURPLE + "★ Legendary Armor"));
                    armorMeta.addEnchant(Enchantment.PROTECTION, 10, true);
                    armorMeta.addEnchant(Enchantment.UNBREAKING, 10, true);
                    armorMeta.addEnchant(Enchantment.MENDING, 1, true);
                    armorMeta.addEnchant(Enchantment.THORNS, 5, true);
                    armorMeta.setUnbreakable(true);
                    armorMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    armor.setItemMeta(armorMeta);
                    player.getInventory().addItem(armor);
                }

                player.sendMessage(ChatColor.GOLD + "🌌 " + ChatColor.WHITE + "The power of " + ChatColor.AQUA + "INFINITY" + ChatColor.WHITE + " flows through you! You received all God Gears!");
                player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 0.5f);
            }

            case "banhammer" -> {
                ItemStack hammer = new ItemStack(Material.MACE, 1);
                ItemMeta meta = hammer.getItemMeta();
                meta.setDisplayName(ChatColor.RED + ChatColor.BOLD.toString() + "⚡ THE BAN HAMMER ⚡");
                meta.setLore(Arrays.asList(
                    "",
                    ChatColor.GRAY + "Owner: " + ChatColor.GOLD + "Krylo_MC",
                    ChatColor.GRAY + "Smite players to permanently ban them!",
                    "",
                    ChatColor.DARK_PURPLE + "★ Divine Weapon"
                ));
                meta.addEnchant(Enchantment.SHARPNESS, 10, true);
                meta.addEnchant(Enchantment.UNBREAKING, 10, true);
                meta.setUnbreakable(true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                hammer.setItemMeta(meta);
                player.getInventory().addItem(hammer);
                player.sendMessage(ChatColor.RED + "🔨 " + ChatColor.WHITE + "You received the divine " + ChatColor.RED + ChatColor.BOLD + "BAN HAMMER" + ChatColor.WHITE + "!");
                player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
            }

            default -> {
                player.sendMessage(ChatColor.RED + "⚠ Unknown item '" + args[0] + "'. Use /customitem to see available items.");
            }
        }

        return true;
    }
}
