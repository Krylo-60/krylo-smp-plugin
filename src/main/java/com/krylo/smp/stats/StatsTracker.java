package com.krylo.smp.stats;

import com.krylo.smp.KryloSMP;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StatsTracker implements Listener {

    private final KryloSMP plugin;
    private final Map<UUID, Long> sessionStarts = new HashMap<>();

    public StatsTracker(KryloSMP plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        
        sessionStarts.put(uuid, System.currentTimeMillis());

        // Increment join count in config
        String basePath = "player-stats." + uuid.toString();
        int joins = plugin.getConfig().getInt(basePath + ".joins", 0) + 1;
        plugin.getConfig().set(basePath + ".joins", joins);
        plugin.getConfig().set(basePath + ".name", player.getName());
        plugin.saveConfig();

        // Push stats updates asynchronously
        pushStatsToDiscord();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (sessionStarts.containsKey(uuid)) {
            long durationSeconds = (System.currentTimeMillis() - sessionStarts.get(uuid)) / 1000;
            sessionStarts.remove(uuid);

            String basePath = "player-stats." + uuid.toString();
            long totalPlaytime = plugin.getConfig().getLong(basePath + ".playtime", 0) + durationSeconds;
            plugin.getConfig().set(basePath + ".playtime", totalPlaytime);
            plugin.saveConfig();
        }

        pushStatsToDiscord();
    }

    public void pushStatsToDiscord() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                ConfigurationSection statsSection = plugin.getConfig().getConfigurationSection("player-stats");
                
                int uniquePlayers = statsSection != null ? statsSection.getKeys(false).size() : 0;
                int totalJoins = 0;
                
                String topPlayerName = "None";
                int maxJoins = 0;

                String topPlaytimePlayerName = "None";
                long maxPlaytime = 0;

                if (statsSection != null) {
                    for (String uuidStr : statsSection.getKeys(false)) {
                        int joins = statsSection.getInt(uuidStr + ".joins", 0);
                        long playtime = statsSection.getLong(uuidStr + ".playtime", 0);
                        String name = statsSection.getString(uuidStr + ".name", "Unknown");

                        totalJoins += joins;

                        if (joins > maxJoins) {
                            maxJoins = joins;
                            topPlayerName = name;
                        }

                        if (playtime > maxPlaytime) {
                            maxPlaytime = playtime;
                            topPlaytimePlayerName = name;
                        }
                    }
                }

                // Call Vercel API to save stats
                URL url = new URL("https://krims-code-chatbot.vercel.app/api/chat");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                // Send action: "update_server_stats"
                String json = "{\n" +
                        "  \"action\": \"update_server_stats\",\n" +
                        "  \"guildId\": \"1420991845546332162\",\n" +
                        "  \"stats\": {\n" +
                        "    \"uniquePlayers\": " + uniquePlayers + ",\n" +
                        "    \"totalJoins\": " + totalJoins + ",\n" +
                        "    \"mostActivePlayer\": \"" + topPlayerName + "\",\n" +
                        "    \"mostActiveJoins\": " + maxJoins + ",\n" +
                        "    \"mostPlaytimePlayer\": \"" + topPlaytimePlayerName + "\",\n" +
                        "    \"mostPlaytimeSeconds\": " + maxPlaytime + "\n" +
                        "  }\n" +
                        "}";

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = json.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                int code = conn.getResponseCode();
                plugin.getLogger().info("[Stats Tracker] Stats updated: " + code);
            } catch (Exception e) {
                plugin.getLogger().warning("[Stats Tracker] Failed to update stats: " + e.getMessage());
            }
        });
    }
}
