package com.krylo.smp.scoreboard;

import com.krylo.smp.KryloSMP;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SMPScoreboard {

    private final KryloSMP plugin;
    private final Map<UUID, Long> sessionStarts = new HashMap<>();

    public SMPScoreboard(KryloSMP plugin) {
        this.plugin = plugin;
        startUpdateTask();
    }

    public void registerPlayer(Player player) {
        sessionStarts.put(player.getUniqueId(), System.currentTimeMillis());
        setScoreboard(player);
    }

    public void unregisterPlayer(Player player) {
        sessionStarts.remove(player.getUniqueId());
    }

    private void startUpdateTask() {
        // Update scoreboard every second (20 ticks)
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                setScoreboard(player);
            }
        }, 0L, 20L);
    }

    private void setScoreboard(Player player) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard board = manager.getNewScoreboard();

        // Title: ⚡ KryloSMP ⚡ (Gold + Cyan Bold)
        String title = "§6⚡ §b§lKryloSMP §6⚡";

        Objective obj = board.registerNewObjective("sidebar", Criteria.DUMMY, title);
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        // Format playtime
        long basePlaytime = plugin.getConfig().getLong("player-stats." + player.getUniqueId().toString() + ".playtime", 0);
        long sessionTime = 0;
        if (sessionStarts.containsKey(player.getUniqueId())) {
            sessionTime = (System.currentTimeMillis() - sessionStarts.get(player.getUniqueId())) / 1000;
        }
        long totalSeconds = basePlaytime + sessionTime;
        String playtimeStr = formatPlaytime(totalSeconds);

        // Cape Status
        String choice = plugin.getConfig().getString("cape-selections." + player.getUniqueId().toString(), "LATER");
        String capeStr = "None";
        if ("NOW".equalsIgnoreCase(choice)) {
            capeStr = "Neon Velocity";
        } else if ("NEVER".equalsIgnoreCase(choice)) {
            capeStr = "Blocked";
        }

        // Add Scoreboard lines (Lines are set in descending order)
        int score = 8;
        
        // Line 8: Separator
        obj.getScore("§7----------------").setScore(score--);
        
        // Line 7: Online Players
        obj.getScore("§f👤 Players: §e" + Bukkit.getOnlinePlayers().size() + " / " + Bukkit.getMaxPlayers()).setScore(score--);
        
        // Line 6: Playtime
        obj.getScore("§f🕒 Playtime: §e" + playtimeStr).setScore(score--);
        
        // Line 5: Active Cape
        obj.getScore("§f🎨 Cape: §b" + capeStr).setScore(score--);
        
        // Line 4: Empty spacer
        obj.getScore("§1 ").setScore(score--);
        
        // Line 3: Connection IP
        obj.getScore("§f📡 IP: §6KryloSmp.play.hosting").setScore(score--);
        
        // Line 2: Separator
        obj.getScore("§7-----------------").setScore(score--);
        
        // Line 1: Footer
        obj.getScore("§d  Built by Krishiv").setScore(score--);

        player.setScoreboard(board);
    }

    private String formatPlaytime(long totalSeconds) {
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        if (hours == 0) {
            return minutes + "m";
        }
        return hours + "h " + minutes + "m";
    }
}
