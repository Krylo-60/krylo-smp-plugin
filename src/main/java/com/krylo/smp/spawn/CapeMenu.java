package com.krylo.smp.spawn;

import com.krylo.smp.KryloSMP;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CapeMenu implements Listener, CommandExecutor {

    private final KryloSMP plugin;
    private final String menuTitle = colorize("&1&lSelect Cape Status");

    public CapeMenu(KryloSMP plugin) {
        this.plugin = plugin;
    }

    public void openMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 9, menuTitle);

        // Fill background with gray glass panes
        ItemStack grayGlass = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 9; i++) {
            inv.setItem(i, grayGlass);
        }

        // Slot 2: [Now] (Cyan concrete)
        ItemStack nowItem = createItem(Material.CYAN_CONCRETE, colorize("&b&l[Now]"),
                colorize("&7Click to get your animated cape immediately!"),
                colorize("&7Grants custom cape permission node."));

        // Slot 4: [Later] (Orange concrete)
        ItemStack laterItem = createItem(Material.ORANGE_CONCRETE, colorize("&e&l[Later]"),
                colorize("&7Decide later. You will receive chat"),
                colorize("&7reminders on joining the server."));

        // Slot 6: [Never] (Red concrete)
        ItemStack neverItem = createItem(Material.RED_CONCRETE, colorize("&c&l[Never]"),
                colorize("&7Disable the cape system. You cannot"),
                colorize("&7access it unless an Admin resets it."));

        inv.setItem(2, nowItem);
        inv.setItem(4, laterItem);
        inv.setItem(6, neverItem);

        player.openInventory(inv);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;
        
        // If they chose Never, check permission override or block
        if (isNever(player.getUniqueId())) {
            player.sendMessage(colorize("&cYou selected Never. To undo this choice and obtain a cape, you must request assistance directly from a Moderator, Admin, or the Owner. Other players cannot grant you access."));
            return true;
        }

        openMenu(player);
        return true;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(menuTitle)) return;
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();

        if (clicked == null || clicked.getType() == Material.AIR) return;

        if (clicked.getType() == Material.CYAN_CONCRETE) {
            player.closeInventory();
            setSelection(player.getUniqueId(), "now");
            
            // Grant permission
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + player.getName() + " permission set cosmetics.cape.custom true");
            player.sendMessage(colorize("&a&l✓ &bCape permission granted! Opening selection menu..."));
            
            // Execute /cosmetics or /cape
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                player.performCommand("cosmetics");
            }, 10L);

            sendDiscordAlert(player.getName(), "Now");

        } else if (clicked.getType() == Material.ORANGE_CONCRETE) {
            player.closeInventory();
            setSelection(player.getUniqueId(), "later");
            
            player.sendMessage(colorize("&eYou selected &lLater&e. You can choose a cape anytime using &b/cape-now&e!"));
            sendDiscordAlert(player.getName(), "Later");

        } else if (clicked.getType() == Material.RED_CONCRETE) {
            player.closeInventory();
            setSelection(player.getUniqueId(), "never");
            
            // Revoke permission just in case
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + player.getName() + " permission unset cosmetics.cape.custom");
            player.sendMessage(colorize("&c&l✗ &7Cape system disabled. To change this choice, contact staff."));
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String choice = getSelection(player.getUniqueId());

        if (choice == null || choice.equals("none")) {
            // First time join - open menu with 1 second delay
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline()) {
                    openMenu(player);
                }
            }, 20L);
        } else if (choice.equals("later")) {
            // Send reminder
            player.sendMessage(colorize("&e&l[!] &7Don't forget! Choose your custom animated cape using &b/cape-now&7!"));
        }
    }

    @EventHandler
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String msg = event.getMessage().toLowerCase();

        if (msg.startsWith("/cape") || msg.startsWith("/cosmetics") || msg.startsWith("/cosmetic")) {
            if (isNever(player.getUniqueId())) {
                event.setCancelled(true);
                player.sendMessage(colorize("&cYou selected Never. To undo this choice and obtain a cape, you must request assistance directly from a Moderator, Admin, or the Owner. Other players cannot grant you access."));
            }
        }
    }

    private void sendDiscordAlert(String playerName, String choice) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                URL url = new URL("https://krims-code-chatbot.vercel.app/api/chat");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                String json = "{\n" +
                        "  \"action\": \"add_broadcast_action\",\n" +
                        "  \"guildId\": \"1420991845546332162\",\n" +
                        "  \"embed\": {\n" +
                        "    \"channelId\": \"default\",\n" +
                        "    \"title\": \"🎴 New Cape Request Pending\",\n" +
                        "    \"description\": \"Player **" + playerName + "** has selected **" + choice + "** on join and is requesting their animated cape configuration!\",\n" +
                        "    \"color\": \"#00f2ff\"\n" +
                        "  }\n" +
                        "}";

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = json.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                int code = conn.getResponseCode();
                plugin.getLogger().info("[Discord Broadcaster] Alert sent: " + code);
            } catch (Exception e) {
                plugin.getLogger().warning("[Discord Broadcaster] Failed to send alert: " + e.getMessage());
            }
        });
    }

    private boolean isNever(UUID uuid) {
        return "never".equals(getSelection(uuid));
    }

    private String getSelection(UUID uuid) {
        return plugin.getConfig().getString("cape-selections." + uuid.toString(), "none");
    }

    private void setSelection(UUID uuid, String status) {
        plugin.getConfig().set("cape-selections." + uuid.toString(), status);
        plugin.saveConfig();
    }

    private ItemStack createItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            List<String> list = new ArrayList<>();
            for (String line : lore) {
                list.add(line);
            }
            meta.setLore(list);
            item.setItemMeta(meta);
        }
        return item;
    }

    private String colorize(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
