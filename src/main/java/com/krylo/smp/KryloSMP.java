package com.krylo.smp;

import com.krylo.smp.commands.*;
import com.krylo.smp.listeners.*;
import com.krylo.smp.spawn.SpawnGenerator;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * KryloSMP — The ultimate all-in-one Minecraft server plugin.
 * Built by Krishiv for krylosmp.aternos.me
 *
 * Features:
 *  - /spawn, /setspawn, /home, /sethome teleportation system
 *  - /tpa, /tpaccept, /tpdeny teleport requests
 *  - Economy system with /balance, /pay, /shop
 *  - Custom legendary weapons via /customitem
 *  - Welcome titles and chat messages on join
 *  - Formatted chat with balance display
 *  - Spawn area protection
 *  - Kill rewards and death penalties
 */
public final class KryloSMP extends JavaPlugin {

    private static KryloSMP instance;

    // Economy storage: UUID -> balance
    private final Map<UUID, Double> balances = new HashMap<>();

    // TPA pending requests: target UUID -> requester UUID
    private final Map<UUID, UUID> tpaRequests = new HashMap<>();

    // Spawn generator
    private SpawnGenerator spawnGenerator;

    // Player join count
    private int totalJoins = 0;

    @Override
    public void onEnable() {
        instance = this;

        // Save default config
        saveDefaultConfig();

        // Load saved economy data
        loadEconomyData();

        // Register all event listeners
        getServer().getPluginManager().registerEvents(new JoinListener(this), this);
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        getServer().getPluginManager().registerEvents(new ProtectionListener(this), this);
        getServer().getPluginManager().registerEvents(new CombatListener(this), this);

        // Register all commands
        SpawnCommand spawnCmd = new SpawnCommand(this);
        getCommand("spawn").setExecutor(spawnCmd);
        getCommand("setspawn").setExecutor(spawnCmd);

        HomeCommand homeCmd = new HomeCommand(this);
        getCommand("home").setExecutor(homeCmd);
        getCommand("sethome").setExecutor(homeCmd);
        getCommand("delhome").setExecutor(homeCmd);

        TpaCommand tpaCmd = new TpaCommand(this);
        getCommand("tpa").setExecutor(tpaCmd);
        getCommand("tpaccept").setExecutor(tpaCmd);
        getCommand("tpdeny").setExecutor(tpaCmd);

        EconomyCommand ecoCmd = new EconomyCommand(this);
        getCommand("balance").setExecutor(ecoCmd);
        getCommand("pay").setExecutor(ecoCmd);
        getCommand("shop").setExecutor(ecoCmd);

        CustomItemCommand ciCmd = new CustomItemCommand(this);
        getCommand("customitem").setExecutor(ciCmd);

        getCommand("krylo").setExecutor((sender, cmd, label, args) -> {
            sender.sendMessage("");
            sender.sendMessage(ChatColor.GOLD + "  ⚡ KryloSMP " + ChatColor.GRAY + "v" + getDescription().getVersion());
            sender.sendMessage(ChatColor.AQUA + "  Built by Krishiv");
            sender.sendMessage(ChatColor.GRAY + "  Server: " + ChatColor.WHITE + "krylosmp.aternos.me:54777");
            sender.sendMessage(ChatColor.GRAY + "  Portal: " + ChatColor.GREEN + "krims-code-portal.vercel.app");
            sender.sendMessage("");
            return true;
        });

        getCommand("buildspawn").setExecutor((sender, cmd, label, args) -> {
            if (!(sender instanceof org.bukkit.entity.Player)) {
                sender.sendMessage("This command can only be used by players.");
                return true;
            }
            org.bukkit.entity.Player p = (org.bukkit.entity.Player) sender;
            if (!p.isOp()) {
                p.sendMessage(ChatColor.RED + "You must be an operator to use this command!");
                return true;
            }
            p.sendMessage(ChatColor.GOLD + "⚡ Regenerating Neon Velocity spawn platform...");
            // Remove old holograms first
            p.getWorld().getEntities().stream()
                .filter(e -> e.getScoreboardTags().contains("krylo_hologram"))
                .forEach(org.bukkit.entity.Entity::remove);
            spawnGenerator.generate(p.getWorld());
            p.sendMessage(ChatColor.GREEN + "✓ Spawn platform rebuilt successfully!");
            return true;
        });

        getLogger().info("═══════════════════════════════════════");
        getLogger().info("  ⚡ KryloSMP v" + getDescription().getVersion() + " ENABLED");
        getLogger().info("  Built by Krishiv — All systems online!");
        getLogger().info("═══════════════════════════════════════");

        // Build spawn platform on first run (delayed to ensure world is loaded)
        spawnGenerator = new SpawnGenerator(this);
        Bukkit.getScheduler().runTaskLater(this, () -> {
            World world = Bukkit.getWorlds().get(0); // Overworld
            if (!spawnGenerator.isSpawnBuilt(world)) {
                spawnGenerator.generate(world);
                getLogger().info("⚡ Neon Velocity spawn platform built!");
            } else {
                getLogger().info("⚡ Spawn platform already exists, skipping generation.");
            }
        }, 40L); // 2 second delay for world load
    }

    @Override
    public void onDisable() {
        saveEconomyData();
        getLogger().info("⚡ KryloSMP disabled. See you next time!");
    }

    // ── Economy Methods ──────────────────────────────────

    public double getBalance(UUID uuid) {
        return balances.getOrDefault(uuid, getConfig().getDouble("starting-balance", 500.0));
    }

    public void setBalance(UUID uuid, double amount) {
        balances.put(uuid, Math.max(0, amount));
        saveEconomyData();
    }

    public void addBalance(UUID uuid, double amount) {
        setBalance(uuid, getBalance(uuid) + amount);
    }

    public boolean removeBalance(UUID uuid, double amount) {
        double current = getBalance(uuid);
        if (current < amount) return false;
        setBalance(uuid, current - amount);
        return true;
    }

    // ── TPA Methods ──────────────────────────────────────

    public Map<UUID, UUID> getTpaRequests() {
        return tpaRequests;
    }

    // ── Join Counter ─────────────────────────────────────

    public int getTotalJoins() {
        return totalJoins;
    }

    public void incrementJoins() {
        totalJoins++;
    }

    // ── Persistence ──────────────────────────────────────

    private void loadEconomyData() {
        if (getConfig().isConfigurationSection("economy-data")) {
            for (String key : getConfig().getConfigurationSection("economy-data").getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(key);
                    double bal = getConfig().getDouble("economy-data." + key);
                    balances.put(uuid, bal);
                } catch (IllegalArgumentException ignored) {}
            }
        }
        totalJoins = getConfig().getInt("total-joins", 0);
    }

    private void saveEconomyData() {
        for (Map.Entry<UUID, Double> entry : balances.entrySet()) {
            getConfig().set("economy-data." + entry.getKey().toString(), entry.getValue());
        }
        getConfig().set("total-joins", totalJoins);
        saveConfig();
    }

    public static KryloSMP getInstance() {
        return instance;
    }

    public SpawnGenerator getSpawnGenerator() {
        return spawnGenerator;
    }
}
