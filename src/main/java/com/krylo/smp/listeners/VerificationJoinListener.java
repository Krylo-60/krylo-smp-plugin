package com.krylo.smp.listeners;

import com.krylo.smp.KryloSMP;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class VerificationJoinListener implements Listener {

    private final KryloSMP plugin;
    private final Set<UUID> pendingPlayers = Collections.synchronizedSet(new HashSet<>());
    private final java.util.Map<UUID, BukkitTask> checkTasks = new ConcurrentHashMap<>();

    public VerificationJoinListener(KryloSMP plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getName();
        UUID uuid = player.getUniqueId();

        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL("https://krims-code-chatbot.vercel.app/api/chat");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setDoOutput(true);

                    String payload = "{\n" +
                            "  \"action\": \"check_join_verification\",\n" +
                            "  \"guildId\": \"1420991845546332162\",\n" +
                            "  \"name\": \"" + playerName + "\"\n" +
                            "}";

                    try (OutputStream os = conn.getOutputStream()) {
                        byte[] input = payload.getBytes(StandardCharsets.UTF_8);
                        os.write(input, 0, input.length);
                    }

                    int responseCode = conn.getResponseCode();
                    if (responseCode == 200) {
                        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                            StringBuilder response = new StringBuilder();
                            String line;
                            while ((line = br.readLine()) != null) {
                                response.append(line.trim());
                            }

                            String resJson = response.toString();
                            boolean isVerified = resJson.contains("\"verified\":true") || resJson.contains("\"verified\": true");
                            boolean isPending = resJson.contains("\"pending\":true") || resJson.contains("\"pending\": true");

                            if (isVerified) {
                                // Verified player - allow play!
                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        player.sendMessage(ChatColor.GREEN + "🟢 " + ChatColor.BOLD + "KryloSMP Gateway: " + ChatColor.GRAY + "Access Granted! Welcome back.");
                                    }
                                }.runTask(plugin);
                            } else if (isPending) {
                                // Unverified player with pending verification request
                                String code = "";
                                int codeIndex = resJson.indexOf("\"code\"");
                                if (codeIndex != -1) {
                                    int startQuote = resJson.indexOf("\"", codeIndex + 6);
                                    if (startQuote != -1) {
                                        int endQuote = resJson.indexOf("\"", startQuote + 1);
                                        if (endQuote != -1) {
                                            code = resJson.substring(startQuote + 1, endQuote);
                                        }
                                    }
                                }

                                if (!code.isEmpty()) {
                                    final String finalCode = code;
                                    pendingPlayers.add(uuid);

                                    new BukkitRunnable() {
                                        @Override
                                        public void run() {
                                            player.sendMessage("");
                                            player.sendMessage(ChatColor.GOLD + "⚡ " + ChatColor.BOLD + "KryloSMP Discord Link Request");
                                            player.sendMessage(ChatColor.GRAY + "We found a pending link request for your account on Discord!");
                                            player.sendMessage(ChatColor.GRAY + "Your verification code is: " + ChatColor.AQUA + ChatColor.BOLD + finalCode);
                                            player.sendMessage(ChatColor.GRAY + "Please enter this code on our Discord server to complete verification!");
                                            player.sendMessage("");
                                            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
                                            
                                            // Show Title Alert
                                            player.sendTitle(ChatColor.GOLD + "Link Your Account", ChatColor.YELLOW + "Code: " + finalCode, 10, 100, 20);
                                        }
                                    }.runTask(plugin);

                                    // Schedule verification checker every 3 seconds
                                    BukkitTask task = new BukkitRunnable() {
                                        int elapsed = 0;
                                        @Override
                                        public void run() {
                                            elapsed += 3;
                                            if (elapsed > 120) { // Kick after 2 mins if they don't verify
                                                new BukkitRunnable() {
                                                    @Override
                                                    public void run() {
                                                        if (player.isOnline()) {
                                                            player.kickPlayer(ChatColor.RED + "❌ Verification Cancelled\n\nYou did not enter the verification code on Discord in time. Please join Discord and try again!");
                                                        }
                                                    }
                                                }.runTask(plugin);
                                                cancelCheckTask(uuid);
                                                return;
                                            }

                                            // Poll Vercel API
                                            try {
                                                URL checkUrl = new URL("https://krims-code-chatbot.vercel.app/api/chat");
                                                HttpURLConnection checkConn = (HttpURLConnection) checkUrl.openConnection();
                                                checkConn.setRequestMethod("POST");
                                                checkConn.setRequestProperty("Content-Type", "application/json");
                                                checkConn.setDoOutput(true);

                                                String checkPayload = "{\n" +
                                                        "  \"action\": \"check_join_verification\",\n" +
                                                        "  \"guildId\": \"1420991845546332162\",\n" +
                                                        "  \"name\": \"" + playerName + "\"\n" +
                                                        "}";

                                                try (OutputStream os = checkConn.getOutputStream()) {
                                                    os.write(checkPayload.getBytes(StandardCharsets.UTF_8));
                                                }

                                                if (checkConn.getResponseCode() == 200) {
                                                    try (BufferedReader checkBr = new BufferedReader(new InputStreamReader(checkConn.getInputStream(), StandardCharsets.UTF_8))) {
                                                        StringBuilder checkResponse = new StringBuilder();
                                                        String checkLine;
                                                        while ((checkLine = checkBr.readLine()) != null) {
                                                            checkResponse.append(checkLine.trim());
                                                        }
                                                        String checkResJson = checkResponse.toString();
                                                        if (checkResJson.contains("\"verified\":true") || checkResJson.contains("\"verified\": true")) {
                                                            // Success!
                                                            pendingPlayers.remove(uuid);
                                                            new BukkitRunnable() {
                                                                @Override
                                                                public void run() {
                                                                    player.sendMessage("");
                                                                    player.sendMessage(ChatColor.GREEN + "🟢 " + ChatColor.BOLD + "KryloSMP Verification SUCCESS!");
                                                                    player.sendMessage(ChatColor.GRAY + "Your account is now linked. Have fun playing!");
                                                                    player.sendMessage("");
                                                                    player.playSound(player.getLocation(), org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
                                                                    player.sendTitle(ChatColor.GREEN + "Verified!", ChatColor.GRAY + "Welcome to KryloSMP", 10, 70, 20);
                                                                }
                                                            }.runTask(plugin);
                                                            cancelCheckTask(uuid);
                                                        }
                                                    }
                                                }
                                            } catch (Exception e) {
                                                // ignore
                                            }
                                        }
                                    }.runTaskTimerAsynchronously(plugin, 60L, 60L); // check every 3s
                                    checkTasks.put(uuid, task);
                                }
                            } else {
                                // Kick immediately
                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        player.kickPlayer(ChatColor.RED + "❌ KryloSMP Security Gate\n\nYou must link your Discord account to play on this server!\n\n" +
                                                ChatColor.GRAY + "1. Join our Discord server: " + ChatColor.AQUA + "https://discord.gg/krylosmp\n" +
                                                ChatColor.GRAY + "2. Go to the " + ChatColor.GOLD + "#🔑verify" + ChatColor.GRAY + " channel and click " + ChatColor.GREEN + "Link Account\n" +
                                                ChatColor.GRAY + "3. Re-join the server to get your code!");
                                    }
                                }.runTask(plugin);
                            }
                        }
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to check join verification: " + e.getMessage());
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        cancelCheckTask(event.getPlayer().getUniqueId());
    }

    private void cancelCheckTask(UUID uuid) {
        pendingPlayers.remove(uuid);
        BukkitTask task = checkTasks.remove(uuid);
        if (task != null) {
            try {
                task.cancel();
            } catch (Exception e) {
                // ignore
            }
        }
    }

    // ═══════════════════════════════════════
    // FREEZE INTERACTION EVENTS
    // ═══════════════════════════════════════

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMove(PlayerMoveEvent event) {
        if (pendingPlayers.contains(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent event) {
        if (pendingPlayers.contains(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "❌ You cannot chat until you link your account on Discord!");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (pendingPlayers.contains(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "❌ You cannot run commands until you link your account on Discord!");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBreak(BlockBreakEvent event) {
        if (pendingPlayers.contains(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlace(BlockPlaceEvent event) {
        if (pendingPlayers.contains(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && pendingPlayers.contains(event.getDamager().getUniqueId())) {
            event.setCancelled(true);
        }
        if (event.getEntity() instanceof Player && pendingPlayers.contains(event.getEntity().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventory(InventoryOpenEvent event) {
        if (pendingPlayers.contains(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }
}
