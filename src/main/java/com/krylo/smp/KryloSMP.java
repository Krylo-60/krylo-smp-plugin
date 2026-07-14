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

    // Scoreboard & Trail Managers
    private com.krylo.smp.scoreboard.SMPScoreboard smpScoreboard;
    private com.krylo.smp.effects.CapeTrail capeTrail;
    
    // Boss Manager
    private com.krylo.smp.boss.CustomBossManager bossManager;

    // Player join count
    private int totalJoins = 0;

    @Override
    public void onEnable() {
        instance = this;
        
        // Initialize Boss Manager
        this.bossManager = new com.krylo.smp.boss.CustomBossManager(this);

        // Initialize Scoreboard & Trail systems
        this.smpScoreboard = new com.krylo.smp.scoreboard.SMPScoreboard(this);
        this.capeTrail = new com.krylo.smp.effects.CapeTrail(this);

        // Register scoreboard event handlers
        getServer().getPluginManager().registerEvents(new org.bukkit.event.Listener() {
            @org.bukkit.event.EventHandler
            public void onJoin(org.bukkit.event.player.PlayerJoinEvent e) {
                smpScoreboard.registerPlayer(e.getPlayer());
            }

            @org.bukkit.event.EventHandler
            public void onQuit(org.bukkit.event.player.PlayerQuitEvent e) {
                smpScoreboard.unregisterPlayer(e.getPlayer());
            }
        }, this);

        // Save default config
        saveDefaultConfig();

        // Load saved economy data
        loadEconomyData();

        // Register all event listeners
        getServer().getPluginManager().registerEvents(new JoinListener(this), this);
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        getServer().getPluginManager().registerEvents(new ProtectionListener(this), this);
        getServer().getPluginManager().registerEvents(new CombatListener(this), this);
        getServer().getPluginManager().registerEvents(new com.krylo.smp.listeners.VerificationJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new com.krylo.smp.listeners.BossCombatListener(this, bossManager), this);
        getServer().getPluginManager().registerEvents(new com.krylo.smp.listeners.PlayerWeaponListener(this), this);
        
        // Register Stats Tracker System
        com.krylo.smp.stats.StatsTracker statsTracker = new com.krylo.smp.stats.StatsTracker(this);
        getServer().getPluginManager().registerEvents(statsTracker, this);
        statsTracker.pushStatsToDiscord();

        // Register RTP (Random Teleport) command + pressure plate listener
        RtpCommand rtpCmd = new RtpCommand(this);
        getCommand("rtp").setExecutor(rtpCmd);
        getServer().getPluginManager().registerEvents(new SpawnInteractionListener(this, rtpCmd), this);

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

        LinkCommand linkCmd = new LinkCommand(this);
        getCommand("link").setExecutor(linkCmd);

        getCommand("summonboss").setExecutor(new com.krylo.smp.commands.SummonBossCommand(bossManager));

        // Register Cape Menu System
        com.krylo.smp.spawn.CapeMenu capeMenu = new com.krylo.smp.spawn.CapeMenu(this);
        getServer().getPluginManager().registerEvents(capeMenu, this);
        getCommand("cape-now").setExecutor(capeMenu);

        getCommand("krylo").setExecutor((sender, cmd, label, args) -> {
            sender.sendMessage("");
            sender.sendMessage(ChatColor.GOLD + "  ⚡ KryloSMP " + ChatColor.GRAY + "v" + getDescription().getVersion());
            sender.sendMessage(ChatColor.AQUA + "  Built by Krishiv");
            sender.sendMessage(ChatColor.GRAY + "  Server: " + ChatColor.WHITE + "KryloSmp.play.hosting");
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
        
        // Register custom player-tier recipes
        registerCustomRecipes();
    }

    @Override
    public void onDisable() {
        if (bossManager != null) {
            bossManager.cleanup();
        }
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

    public com.krylo.smp.boss.CustomBossManager getBossManager() {
        return bossManager;
    }

    private void registerCustomRecipes() {
        // 1. Shadow Blade
        org.bukkit.inventory.ItemStack shadowBlade = new org.bukkit.inventory.ItemStack(org.bukkit.Material.NETHERITE_SWORD);
        org.bukkit.inventory.meta.ItemMeta meta1 = shadowBlade.getItemMeta();
        meta1.setDisplayName(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Shadow Blade");
        java.util.List<String> lore1 = new java.util.ArrayList<>();
        lore1.add(ChatColor.GRAY + "A legendary sword forged from the heart of shadows.");
        lore1.add("");
        lore1.add(ChatColor.LIGHT_PURPLE + "⭐ Passive: Speed I");
        lore1.add(ChatColor.LIGHT_PURPLE + "⚔️ On Hit: 10% chance to Blind target (3s)");
        meta1.setLore(lore1);
        shadowBlade.setItemMeta(meta1);

        org.bukkit.NamespacedKey key1 = new org.bukkit.NamespacedKey(this, "shadow_blade");
        org.bukkit.inventory.ShapedRecipe recipe1 = new org.bukkit.inventory.ShapedRecipe(key1, shadowBlade);
        recipe1.shape(" N ", " C ", " S ");
        recipe1.setIngredient('N', org.bukkit.Material.NETHERITE_INGOT);
        recipe1.setIngredient('C', org.bukkit.Material.HEART_OF_THE_SEA); // Shadow Core
        recipe1.setIngredient('S', org.bukkit.Material.STICK);
        Bukkit.addRecipe(recipe1);

        // 2. Blossom Shield
        org.bukkit.inventory.ItemStack blossomShield = new org.bukkit.inventory.ItemStack(org.bukkit.Material.SHIELD);
        org.bukkit.inventory.meta.ItemMeta meta2 = blossomShield.getItemMeta();
        meta2.setDisplayName(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Blossom Shield");
        java.util.List<String> lore2 = new java.util.ArrayList<>();
        lore2.add(ChatColor.GRAY + "An ancient shield infused with Cherry Elder petals.");
        lore2.add("");
        lore2.add(ChatColor.LIGHT_PURPLE + "🛡️ Blocking: Restores 1 Heart (10s cooldown)");
        meta2.setLore(lore2);
        blossomShield.setItemMeta(meta2);

        org.bukkit.NamespacedKey key2 = new org.bukkit.NamespacedKey(this, "blossom_shield");
        org.bukkit.inventory.ShapedRecipe recipe2 = new org.bukkit.inventory.ShapedRecipe(key2, blossomShield);
        recipe2.shape(" P ", " S ", " P ");
        recipe2.setIngredient('P', org.bukkit.Material.PINK_PETALS); // Elder Blossom
        recipe2.setIngredient('S', org.bukkit.Material.SHIELD);
        Bukkit.addRecipe(recipe2);

        // 3. Vampiric Dagger
        org.bukkit.inventory.ItemStack vampDagger = new org.bukkit.inventory.ItemStack(org.bukkit.Material.IRON_SWORD);
        org.bukkit.inventory.meta.ItemMeta meta3 = vampDagger.getItemMeta();
        meta3.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "Vampiric Dagger");
        java.util.List<String> lore3 = new java.util.ArrayList<>();
        lore3.add(ChatColor.GRAY + "A cursed dagger that thirsts for blood.");
        lore3.add("");
        lore3.add(ChatColor.LIGHT_PURPLE + "🩸 On Hit: Steals 10% of damage dealt as health");
        meta3.setLore(lore3);
        vampDagger.setItemMeta(meta3);

        org.bukkit.NamespacedKey key3 = new org.bukkit.NamespacedKey(this, "vampiric_dagger");
        org.bukkit.inventory.ShapedRecipe recipe3 = new org.bukkit.inventory.ShapedRecipe(key3, vampDagger);
        recipe3.shape(" G ", " C ", " S ");
        recipe3.setIngredient('G', org.bukkit.Material.GHAST_TEAR);
        recipe3.setIngredient('C', org.bukkit.Material.HEART_OF_THE_SEA); // Shadow Core
        recipe3.setIngredient('S', org.bukkit.Material.STICK);
        Bukkit.addRecipe(recipe3);
    }
}
