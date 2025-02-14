package tk.shanebee.hg.data;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import tk.shanebee.hg.HG;
import tk.shanebee.hg.util.Util;
import tk.shanebee.hg.util.Vault;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Main config class <b>Internal Use Only</b>
 */
public class Config {

    public static boolean debug;
    public static boolean practiceMode;

    //Basic settings
    public static boolean broadcastJoinMessages;
    public static boolean broadcastWinMessages;
    public static boolean economy = true;
    public static boolean spawnmobs;
    public static int spawnmobsinterval;
    public static boolean bossbar;
    public static int trackingstickuses;
    public static int playersfortrackingstick;
    public static int maxchestcontent;
    public static int minchestcontent;
    public static int maxbonuscontent;
    public static int minbonuscontent;
    public static boolean teleportEnd;
    public static int teleportEndTime;
    public static List<String> bonusBlockTypes;
    public static boolean hideNametags;
    public static boolean savePreviousLocation;
    public static int timerInterval;
    public static String lobbyLocationStr;
    public static Location lobbyLocation;
    public static double explosionMultiplier;

    //Team info
    public static boolean team_showTeamNames;
    public static int team_maxTeamSize;
    public static boolean team_friendly_fire;
    public static boolean team_see_invis;
    public static int total_team_count;

    //Reward info
    public static boolean giveReward;
    public static int cash;
    public static List<String> rewardCommands;
    public static List<String> rewardMessages;

    //Rollback config info
    public static boolean breakblocks;
    public static int blocks_per_second;
    public static boolean protectCooldown;
    public static boolean fixleaves;
    public static boolean preventtrample;
    public static List<String> blocks;
    public static boolean itemframe_take;

    //Random chest
    public static boolean randomChest;
    public static int randomChestInterval;
    public static int randomChestMaxContent;

    //World border
    public static boolean borderEnabled;
    public static boolean borderOnStart;
    public static boolean centerSpawn;
    public static int borderCountdownStart;
    public static int borderCountdownEnd;
    public static int borderFinalSize;
    public static int gasStartTime;

    //Spectate
    public static boolean spectateEnabled;
    public static boolean spectateOnDeath;
    public static boolean spectateHide;
    public static boolean spectateFly;
    public static boolean spectateChat;

    //mcMMO
    public static boolean mcmmoUseSkills;
    public static boolean mcmmoGainExp;

    //chest placement
    public static int globalChestChance;

    //Command tab completions
    public static ConfigurationSection availableCommands;

    //For points
    public static int pointsPerKill;
    public static int pointsPerTeamBonus;
    public static Map<Integer, Integer> pointsPerPlacement = new HashMap<>();

    public static ArrayList<Material> chestDropLandBlocks = new ArrayList<>();

    private final HG plugin;
    private File configFile;
    private FileConfiguration config;

    public Config(HG plugin) {
        this.plugin = plugin;
        loadConfigFile();
    }

    private void loadConfigFile() {
        if (configFile == null) {
            configFile = new File(plugin.getDataFolder(), "config.yml");
        }
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
            Util.log("&7New config.yml created");
        }
        config = YamlConfiguration.loadConfiguration(configFile);
        matchConfig(config, configFile);
        loadConfig();
        Util.log("&7config.yml loaded");
    }


    private void loadConfig() {
        debug = config.getBoolean("settings.debug");
        practiceMode = config.getBoolean("settings.practice-mode");
        broadcastJoinMessages = config.getBoolean("settings.broadcast-join-messages");
        broadcastWinMessages = config.getBoolean("settings.broadcast-win-messages");
        spawnmobs = config.getBoolean("settings.spawn-mobs");
        spawnmobsinterval = config.getInt("settings.spawn-mobs-interval") * 20;
        bossbar = config.getBoolean("settings.bossbar-countdown");
        trackingstickuses = config.getInt("settings.trackingstick-uses");
        playersfortrackingstick = config.getInt("settings.players-for-trackingstick");
        maxchestcontent = config.getInt("settings.max-chestcontent");
        minchestcontent = config.getInt("settings.min-chestcontent");
        maxbonuscontent = config.getInt("settings.max-bonus-chestcontent");
        minbonuscontent = config.getInt("settings.min-bonus-chestcontent");
        hideNametags = config.getBoolean("settings.hide-nametags");
        savePreviousLocation = config.getBoolean("settings.save-previous-location");
        bonusBlockTypes = config.getStringList("settings.bonus-block-types");
        timerInterval = config.getInt("settings.timer-interval");
        lobbyLocationStr = config.getString("settings.join-location");
        explosionMultiplier = config.getDouble("settings.explosion-multiplier");

        // Team
        team_maxTeamSize = config.getInt("team.max-team-size");
        team_showTeamNames = config.getBoolean("team.show-team-nametags");
        team_friendly_fire = config.getBoolean("team.allow-friendly-fire");
        team_see_invis = config.getBoolean("team.can-see-friendly-invisibles");
        total_team_count = config.getInt("team.total-team-count");

        giveReward = config.getBoolean("reward.enabled");
        cash = config.getInt("reward.cash");
        rewardCommands = config.getStringList("reward.commands");
        rewardMessages = config.getStringList("reward.messages");
        giveReward = config.getBoolean("reward.enabled");
        cash = config.getInt("reward.cash");
        breakblocks = config.getBoolean("rollback.allow-block-break");
        blocks_per_second = config.getInt("rollback.blocks-per-second");
        protectCooldown = config.getBoolean("rollback.protect-during-cooldown");
        fixleaves = config.getBoolean("rollback.fix-leaves");
        preventtrample = config.getBoolean("rollback.prevent-trampling");
        blocks = config.getStringList("rollback.editable-blocks");
        itemframe_take = config.getBoolean("rollback.allow-itemframe-take");

        randomChest = config.getBoolean("random-chest.enabled");
        randomChestInterval = config.getInt("random-chest.interval") * 20;
        randomChestMaxContent = config.getInt("random-chest.max-chestcontent");
        teleportEnd = config.getBoolean("settings.teleport-at-end");
        teleportEndTime = config.getInt("settings.teleport-at-end-time");

        borderEnabled = config.getBoolean("world-border.enabled");
        borderOnStart = config.getBoolean("world-border.initiate-on-start");
        centerSpawn = config.getBoolean("world-border.center-on-first-spawn");
        borderCountdownStart = config.getInt("world-border.countdown-start");
        borderCountdownEnd = config.getInt("world-border.countdown-end");
        borderFinalSize = config.getInt("world-border.final-border-size");
        gasStartTime = config.getInt("gas-start-time");

        spectateEnabled = config.getBoolean("spectate.enabled");
        spectateOnDeath = config.getBoolean("spectate.death-to-spectate");
        spectateHide = config.getBoolean("spectate.hide-spectators");
        spectateFly = config.getBoolean("spectate.fly");
        spectateChat = config.getBoolean("spectate.chat");

        mcmmoUseSkills = config.getBoolean("mcmmo.use-skills");
        mcmmoGainExp = config.getBoolean("mcmmo.gain-experience");

        // chest chance
        globalChestChance = config.getInt("chestspawning.globalchance");

        availableCommands = config.getConfigurationSection("available-commands");

        pointsPerKill = config.getInt("points.per-kill");
        pointsPerTeamBonus = config.getInt("points.per-team-bonus");

        List<Integer> placementPoints = config.getIntegerList("points.placement");
        for (int i = 1; i <= Config.total_team_count; i++) {
            pointsPerPlacement.put(i, (placementPoints.size() > i ? placementPoints.get(i - 1) : 0));
        }

        List<String> allowedLanding = config.getStringList("random-chest.allowed-landing");
        for (String allowedString : allowedLanding) {
            Material mat = Material.getMaterial(allowedString);
            if (mat == null) continue;

            if (!chestDropLandBlocks.contains(mat))
                chestDropLandBlocks.add(mat);
        }

        try {
            Vault.setupEconomy();
            if (Vault.economy == null) {
                Util.log("&cUnable to setup vault!");
                Util.log(" - &cEconomy provider is missing.");
                Util.log(" - Cash rewards will not be given out..");
                giveReward = false;
                economy = false;
            }
        } catch (NoClassDefFoundError e) {
            Util.log("&cUnable to setup vault!");
            Util.log("  - Cash rewards will not be given out..");
            giveReward = false;
            economy = false;
        }
    }

    // Used to update config
    @SuppressWarnings("ConstantConditions")
    private void matchConfig(FileConfiguration config, File file) {
        try {
            boolean hasUpdated = false;
            InputStream test = plugin.getResource(file.getName());
            assert test != null;
            InputStreamReader is = new InputStreamReader(test);
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(is);
            for (String key : defConfig.getConfigurationSection("").getKeys(true)) {
                if (!config.contains(key) && !key.contains("kits")) {
                    config.set(key, defConfig.get(key));
                    hasUpdated = true;
                }
            }
            for (String key : config.getConfigurationSection("").getKeys(true)) {
                if (!defConfig.contains(key) && !key.contains("kits.") && !key.contains("globalexit")) {
                    config.set(key, null);
                    hasUpdated = true;
                }
            }
            if (hasUpdated)
                config.save(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Configuration getConfig() {
        return config;
    }

    public void save() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
