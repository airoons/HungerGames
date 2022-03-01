package tk.shanebee.hg.game;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import tk.shanebee.hg.HG;
import tk.shanebee.hg.PointType;
import tk.shanebee.hg.Status;
import tk.shanebee.hg.data.Config;
import tk.shanebee.hg.data.Language;
import tk.shanebee.hg.data.Leaderboard;
import tk.shanebee.hg.data.PlayerData;
import tk.shanebee.hg.events.GameEndEvent;
import tk.shanebee.hg.events.GameStartEvent;
import tk.shanebee.hg.game.GameCommandData.CommandType;
import tk.shanebee.hg.managers.KitManager;
import tk.shanebee.hg.managers.MobManager;
import tk.shanebee.hg.managers.PlayerManager;
import tk.shanebee.hg.tasks.ChestDropTask;
import tk.shanebee.hg.tasks.FreeRoamTask;
import tk.shanebee.hg.tasks.Rollback;
import tk.shanebee.hg.tasks.SpawnerTask;
import tk.shanebee.hg.tasks.StartingTask;
import tk.shanebee.hg.tasks.TimerTask;
import tk.shanebee.hg.util.Util;
import tk.shanebee.hg.util.Vault;

import java.util.*;

/**
 * General game object
 */
@SuppressWarnings("unused")
public class Game {

    final HG plugin;
    final Language lang;

    // Managers
    KitManager kitManager;
    private final MobManager mobManager;
    private final PlayerManager playerManager;

    // Task ID's here!
    private SpawnerTask spawner;
    private FreeRoamTask freeRoam;
    private StartingTask starting;
    private TimerTask timer;
    private ChestDropTask chestDrop;

    // Data Objects
    final GameArenaData gameArenaData;
    final GameBarData bar;
    final GamePlayerData gamePlayerData;
    final GameBlockData gameBlockData;
    final GameItemData gameItemData;
    final GameCommandData gameCommandData;
    final GameBorderData gameBorderData;
    final GamePointData gamePointData;
    final GameTeamData gameTeamData;

    public boolean gracePeriod;
    private boolean gameEnded;

    /**
     * Create a new game
     * <p>Internally used when loading from config on server start</p>
     *
     * @param name       Name of this game
     * @param bound      Bounding region of this game
     * @param spawns     List of spawns for this game
     * @param lobbySign  Lobby sign block
     * @param timer      Length of the game (in seconds)
     * @param minPlayers Minimum players to be able to start the game
     * @param maxPlayers Maximum players that can join this game
     * @param roam       Roam time for this game
     * @param isReady    If the game is ready to start
     * @param cost       Cost of this game
     */
    public Game(String name, Bound bound, List<Location> spawns, List<ChestData> chests, Sign lobbySign, int timer, int minPlayers, int maxPlayers, int countDownTime, int roam, boolean isReady, int cost) {
        this(name, bound, timer, minPlayers, maxPlayers, countDownTime, roam, cost);
        gameArenaData.spawns.addAll(spawns);
        gameArenaData.chests.addAll(chests);
        this.gameBlockData.sign1 = lobbySign;

        // If lobby signs are not properly setup, game is not ready
        if (!this.gameBlockData.setLobbyBlock(lobbySign)) {
            isReady = false;
        }
        gameArenaData.setStatus(isReady ? Status.READY : Status.BROKEN);

        this.kitManager = plugin.getKitManager();

        new BukkitRunnable() {
            @Override
            public void run() {
                gameArenaData.updateBoards();
            }
        }.runTaskTimer(plugin, 0L, 10L);
    }

    /**
     * Create a new game
     * <p>Internally used when creating a game with the <b>/hg create</b> command</p>
     *
     * @param name       Name of this game
     * @param bound      Bounding region of this game
     * @param timer      Length of the game (in seconds)
     * @param minPlayers Minimum players to be able to start the game
     * @param maxPlayers Maximum players that can join this game
     * @param countDownTime Time before game starts once all players join
     * @param roam       Roam time for this game
     * @param cost       Cost of this game
     */
    public Game(String name, Bound bound, int timer, int minPlayers, int maxPlayers, int countDownTime, int roam, int cost) {
        this.plugin = HG.getPlugin();
        this.gameArenaData = new GameArenaData(this, name, bound, timer, minPlayers, maxPlayers, countDownTime, roam, cost);
        this.gameArenaData.status = Status.NOTREADY;
        this.playerManager = HG.getPlugin().getPlayerManager();
        this.lang = plugin.getLang();
        this.kitManager = plugin.getKitManager();
        this.mobManager = new MobManager(this);
        this.bar = new GameBarData(this);
        this.gamePlayerData = new GamePlayerData(this);
        this.gameTeamData = new GameTeamData(this);
        this.gameBlockData = new GameBlockData(this);
        this.gameItemData = new GameItemData(this);
        this.gameCommandData = new GameCommandData(this);
        this.gameBorderData = new GameBorderData(this);
        this.gameBorderData.setBorderSize(Config.borderFinalSize);
        this.gameBorderData.setBorderTimer(Config.borderCountdownStart, Config.borderCountdownEnd);
        this.gamePointData = new GamePointData(this);
        this.gameEnded = false;
    }

    /**
     * Get an instance of the GameArenaData
     *
     * @return Instance of GameArenaData
     */
    public GameArenaData getGameArenaData() {
        return gameArenaData;
    }

    /**
     * Get an instance of the GameBarData
     *
     * @return Instance of GameBarData
     */
    public GameBarData getGameBarData() {
        return bar;
    }

    /**
     * Get an instance of the GamePlayerData
     *
     * @return Instance of GamePlayerData
     */
    public GamePlayerData getGamePlayerData() {
        return gamePlayerData;
    }

    /**
     * Get an instance of the GameBlockData
     *
     * @return Instance of GameBlockData
     */
    public GameBlockData getGameBlockData() {
        return gameBlockData;
    }

    /**
     * Get an instance of the GameItemData
     *
     * @return Instance of GameItemData
     */
    public GameItemData getGameItemData() {
        return gameItemData;
    }

    /**
     * Get an instance of the GameCommandData
     *
     * @return Instance of GameCommandData
     */
    public GameCommandData getGameCommandData() {
        return gameCommandData;
    }

    /**
     * Get an instance of the GameBorderData
     *
     * @return Instance of GameBorderData
     */
    public GameBorderData getGameBorderData() {
        return gameBorderData;
    }

    /**
     * Get an instance of GamePointData
     *
     * @return Instance of GamePointData
     */
    public GamePointData getGamePointData() {
        return gamePointData;
    }

    public GameTeamData getGameTeamData() {
        return gameTeamData;
    }

    public StartingTask getStartingTask() {
        return this.starting;
    }

    /**
     * Get the location of the lobby for this game
     *
     * @return Location of the lobby sign
     */
    public Location getLobbyLocation() {
        return gameBlockData.sign1.getLocation();
    }

    /**
     * Get the kits for this game
     *
     * @return The KitManager kit for this game
     */
    public KitManager getKitManager() {
        return this.kitManager;
    }

    /**
     * Set the kits for this game
     *
     * @param kit The KitManager kit to set
     */
    @SuppressWarnings("unused")
    public void setKitManager(KitManager kit) {
        this.kitManager = kit;
    }

    /**
     * Get this game's MobManager
     *
     * @return MobManager for this game
     */
    public MobManager getMobManager() {
        return this.mobManager;
    }

    /**
     * Start the pregame countdown
     */
    public void startPreGame() {
        // Call the GameStartEvent
        GameStartEvent event = new GameStartEvent(this);
        Bukkit.getPluginManager().callEvent(event);

        gameArenaData.status = Status.COUNTDOWN;
        starting = new StartingTask(this);
        gameBlockData.updateLobbyBlock();
        gamePointData.resetAll();
    }

    /**
     * Start the free roam state of the game
     */
    public void startFreeRoam() {
        gameArenaData.status = Status.BEGINNING;
        gamePlayerData.applyKits();
        gameBlockData.updateLobbyBlock();
        gameArenaData.bound.removeEntities();
        freeRoam = new FreeRoamTask(this);
        gameCommandData.runCommands(CommandType.START, null);
    }

    /**
     * Start the game
     */
    public void startGame() {
        gamePointData.resetAll();
        gameArenaData.status = Status.RUNNING;
        if (Config.spawnmobs) spawner = new SpawnerTask(this, Config.spawnmobsinterval);
        if (Config.randomChest) chestDrop = new ChestDropTask(this);
        gameBlockData.updateLobbyBlock();
        if (Config.bossbar) {
            bar.createBossbar(gameArenaData.timer);
        }
        if (Config.borderEnabled && Config.borderOnStart) {
            gameBorderData.setBorder(gameArenaData.timer);
        }

        for (UUID uuid : gamePlayerData.getPlayersAndSpectators()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                player.setLevel(0);
                gameArenaData.boards.fullUpdate(player);
            }
        }

        for (Team team : gameTeamData.getTeams()) {
            if (team == null) continue;
            for (UUID uuid : team.getPlayers()) {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null) {
                    Util.log("Team #" + team.getId() + ": " + player.getName());
                }
            }
        }

        timer = new TimerTask(this, gameArenaData.timer);
    }

    public void cancelTasks() {
        if (spawner != null) spawner.stop();
        if (timer != null) timer.stop();
        if (starting != null) starting.stop();
        if (freeRoam != null) freeRoam.stop();
        if (chestDrop != null) chestDrop.shutdown();
    }

    /**
     * Stop the game
     */
    public void stop() {
        stop(false);
    }

    /**
     * Stop the game
     *
     * @param death Whether the game stopped after the result of a death (false = no winnings payed out)
     */
    public void stop(Boolean death) {
        if (gameEnded)
            return;
        gameEnded = true;

        for (Team aTeam : gameTeamData.getTeams()) {
            if (aTeam.isAlive()) {
                gamePointData.setPlacement(1);
                gamePointData.addGamePoints(aTeam, PointType.PLACEMENT);
                break;
            }
        }

        List<UUID> win = new ArrayList<>();
        Team winnerTeam = gamePointData.getWinnerTeam();

        if (winnerTeam != null) {
            for (UUID uuid : winnerTeam.getPlayers()) {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null) {
                    gamePlayerData.heal(player);
                }
                win.add(uuid);
            }
        }

        gamePointData.printDebugLog();

        String winner = Util.translateStop(Util.convertUUIDListToStringList(win));

        // Broadcast wins
        String broadcast = lang.player_won.replace("<arena>", gameArenaData.name).replace("<winner>", winner);
        if (Config.broadcastWinMessages) {
            String title = Util.getColString(lang.game_over);
            String subtitle = Util.getColString(broadcast);
            for (UUID u : gamePlayerData.getPlayersAndSpectators()) {
                Player p = Bukkit.getPlayer(u);
                if (p != null)
                    p.sendTitle(title, subtitle, 5, 100, 5);
            }
        } else {
            gamePlayerData.msgAllPlayers(broadcast);
        }
        gamePlayerData.soundAll(Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);

        cancelTasks();

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (UUID uuid : gamePlayerData.players) {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null) {
                    PlayerData playerData = playerManager.getPlayerData(uuid);
                    Location previousLocation = playerData.getPreviousLocation();

                    gamePlayerData.heal(player);
                    playerData.restore(player);
                    gamePlayerData.exit(player, previousLocation);
                    playerManager.removePlayerData(uuid);
                }
            }

            if (Config.borderEnabled) {
                gameBorderData.resetBorder();
            }

            gameArenaData.bound.removeEntities();

            for (UUID uuid : gamePlayerData.getSpectators()) {
                Player spectator = Bukkit.getPlayer(uuid);
                if (spectator != null) {
                    gamePlayerData.leaveSpectate(spectator);
                }
            }

            if (gameArenaData.status == Status.RUNNING) {
                bar.clearBar();
            }

            if (!win.isEmpty() && death) {
                double db = (double) Config.cash / win.size();
                for (UUID u : win) {
                    if (Config.giveReward) {
                        Player p = Bukkit.getPlayer(u);
                        assert p != null;
                        if (!Config.rewardCommands.isEmpty()) {
                            for (String cmd : Config.rewardCommands) {
                                if (!cmd.equalsIgnoreCase("none"))
                                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("<player>", p.getName()));
                            }
                        }
                        if (!Config.rewardMessages.isEmpty()) {
                            for (String msg : Config.rewardMessages) {
                                if (!msg.equalsIgnoreCase("none"))
                                    Util.scm(p, msg.replace("<player>", p.getName()));
                            }
                        }
                        if (Config.cash != 0) {
                            Vault.economy.depositPlayer(Bukkit.getServer().getOfflinePlayer(u), db);
                            Util.scm(p, lang.winning_amount.replace("<amount>", String.valueOf(db)));
                        }
                    }
                    plugin.getLeaderboard().addStat(u, Leaderboard.Stats.WINS);
                    plugin.getLeaderboard().addStat(u, Leaderboard.Stats.GAMES);
                }
            }
            gameBlockData.clearChests();

            if (gameBlockData.requiresRollback()) {
                if (plugin.isEnabled()) {
                    new Rollback(this);
                } else {
                    // Force rollback if server is stopping
                    gameBlockData.forceRollback();
                }
            } else {
                gameArenaData.status = Status.READY;
                gameBlockData.updateLobbyBlock();
            }
            gameArenaData.updateBoards();

            gameCommandData.runCommands(CommandType.STOP, null);

            // Call GameEndEvent
            Collection<Player> winners = new ArrayList<>();
            for (UUID uuid : win) {
                winners.add(Bukkit.getPlayer(uuid));
            }

            // Game has ended, we can clear all players now
            gamePlayerData.clearPlayers();
            gamePlayerData.clearSpectators();
            resetRandomChests();
            Bukkit.getPluginManager().callEvent(new GameEndEvent(this, winners, death));

            gameArenaData.timeLeft = "00:00";
            gameEnded = false;

            for (Player oPlayer : Bukkit.getOnlinePlayers())
                for (Player otPlayer : Bukkit.getOnlinePlayers())
                if (playerManager.getGame(oPlayer) == null && playerManager.getGame(otPlayer) == null) {
                    oPlayer.showPlayer(plugin, otPlayer);
                    otPlayer.showPlayer(plugin, oPlayer);
                }

            if (Config.practiceMode)
                gameTeamData.resetTeams();
        }, 200);
    }

    void updateAfterDeath(Player player, boolean death) {
        Status status = gameArenaData.status;
        if (status == Status.RUNNING || status == Status.BEGINNING || status == Status.COUNTDOWN) {
            if (isGameOver()) {
                if (!death) {
                    for (UUID uuid : gamePlayerData.players) {
                        if (gamePlayerData.kills.get(uuid) >= 1) {
                            death = true;
                        }
                    }
                }
                boolean finalDeath = death;
                if (plugin.isEnabled()) {
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        stop(finalDeath);
                        gameBlockData.updateLobbyBlock();
                        gameArenaData.updateBoards();
                    }, 20);
                } else {
                    stop(finalDeath);
                }

            }
        } else if (status == Status.WAITING) {
            gamePlayerData.msgAll(lang.player_left_game
                    .replace("<arena>", gameArenaData.getName())
                    .replace("<player>", player.getName()));
        }
        gameBlockData.updateLobbyBlock();
        gameArenaData.updateBoards();
    }

    public void resetRandomChests() {
        Random rand = new Random();
        World world = gameArenaData.getBound().getWorld();

        Block block = null;
        Directional dir;

        int spawnChance = Config.globalChestChance;

        for (ChestData chestData : gameArenaData.chests) {
            block = world.getBlockAt(chestData.getLocation());

            if (rand.nextInt(100) <= spawnChance) {
                if (block.getType() != Material.CHEST) {
                    block.setType(Material.CHEST);
                    dir = (Directional) block.getBlockData();
                    dir.setFacing(chestData.getBlockFace());
                    block.setBlockData(dir);
                }
            } else if (block.getType() == Material.CHEST)
                block.setType(Material.AIR);
        }
    }

    boolean isGameOver() {
        if (gamePlayerData.players.size() <= 1) return true;
        for (UUID uuid : gamePlayerData.players) {
            Team team = gameTeamData.getTeamData(uuid).getTeam();

            if (team != null && (team.getPlayers().size() >= gamePlayerData.players.size())) {
                for (UUID u : gamePlayerData.players) {
                    if (!team.getPlayers().contains(u)) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    public TimerTask getTimer() {
        return timer;
    }

    public ChestDropTask getChestDrop() {
        return chestDrop;
    }

    @Override
    public String toString() {
        return "Game{name='" + gameArenaData.name + '\'' + ", bound=" + gameArenaData.bound + '}';
    }

}
