package tk.shanebee.hg.game;

import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;
import tk.shanebee.hg.Status;
import tk.shanebee.hg.data.Config;
import tk.shanebee.hg.data.PlayerData;
import tk.shanebee.hg.data.TeamData;
import tk.shanebee.hg.events.PlayerJoinGameEvent;
import tk.shanebee.hg.events.PlayerLeaveGameEvent;
import tk.shanebee.hg.game.GameCommandData.CommandType;
import tk.shanebee.hg.gui.SpectatorGUI;
import tk.shanebee.hg.gui.TeamGUI;
import tk.shanebee.hg.managers.KitManager;
import tk.shanebee.hg.managers.PlayerManager;
import tk.shanebee.hg.util.Util;
import tk.shanebee.hg.util.Vault;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Data class for holding a {@link Game Game's} players
 */
public class GamePlayerData extends Data {

    private final PlayerManager playerManager;
    private final SpectatorGUI spectatorGUI;

    // Player Lists
    final List<UUID> players = new ArrayList<>();
    final List<UUID> spectators = new ArrayList<>();

    // Data lists
    final Map<UUID, Integer> kills = new HashMap<>();
    final Map<UUID, Integer> chestsLooted = new HashMap<>();

    protected GamePlayerData(Game game) {
        super(game);
        this.playerManager = plugin.getPlayerManager();
        this.spectatorGUI = new SpectatorGUI(game);
    }

    /**
     * Get a list of all players in the game
     *
     * @return UUID list of all players in game
     */
    public List<UUID> getPlayers() {
        return players;
    }

    void clearPlayers() {
        players.clear();
    }

    /**
     * Get a list of all players currently spectating the game
     *
     * @return List of spectators
     */
    public List<UUID> getSpectators() {
        return new ArrayList<>(this.spectators);
    }

    public List<UUID> getPlayersAndSpectators() {
        return Stream.concat(players.stream(), spectators.stream()).distinct()
                .collect(Collectors.toList());
    }

    void clearSpectators() {
        spectators.clear();
    }

    public SpectatorGUI getSpectatorGUI() {
        return spectatorGUI;
    }

    // Utility methods

    private void kitHelp(Player player) {
        // Clear the chat a little bit, making this message easier to see
        for (int i = 0; i < 20; ++i)
            Util.scm(player, " ");
        Util.scm(player, lang.kit_join_header);
        if (player.hasPermission("hg.kit") && game.kitManager.hasKits()) {
            Util.scm(player, lang.kit_join_msg);
            Util.scm(player, lang.kit_join_avail.replace("<arena-name>", game.gameArenaData.customName).replace( "<author>", game.gameArenaData.author));
        }
        Util.scm(player, lang.kit_join_footer);
    }

    /**
     * Apply kits to players in game
     */
    public void applyKits() {
        KitManager kitManager = game.kitManager;
        players.forEach(uuid -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                kitManager.applyKit(player);
            }
        });
        // Clean up kits now that they're all applied
        kitManager.resetPlayerKits();
    }

    /**
     * Respawn all players in the game back to spawn points
     */
    public void respawnAll() {
        for (UUID u : players) {
            Player p = Bukkit.getPlayer(u);
            if (p != null)
                p.teleport(getSpawn(game.gameTeamData.getTeamData(u).getTeam()));
        }
    }

    void heal(Player player) {
        for (PotionEffect ef : player.getActivePotionEffects()) {
            player.removePotionEffect(ef.getType());
        }
        player.closeInventory();
        player.setHealth(20);
        Bukkit.getPluginManager().callEvent(new EntityRegainHealthEvent(player, 0, EntityRegainHealthEvent.RegainReason.CUSTOM));
        player.setFoodLevel(20);
        try {
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> player.setFireTicks(0), 1);
        } catch (IllegalPluginAccessException ignore) {
        }
    }

    /**
     * Freeze a player
     *
     * @param player Player to freeze
     */
    public void freeze(Player player) {
        player.setGameMode(GameMode.SURVIVAL);
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 23423525, -10, false, false));
        player.setWalkSpeed(0.0001F);
        player.setFoodLevel(1);
        player.setAllowFlight(false);
        player.setFlying(false);
        player.setInvulnerable(true);
    }

    /**
     * Unfreeze a player
     *
     * @param player Player to unfreeze
     */
    public void unFreeze(Player player) {
        player.removePotionEffect(PotionEffectType.JUMP);
        player.setWalkSpeed(0.2F);
    }

    /**
     * Send a message to all players in the game
     * <p>Prefix will be included</p>
     *
     * @param message Message to send
     */
    public void msgAllPlayers(String message) {
        msgAllPlayers(message, true);
    }

    /**
     * Send a message to all players in the game
     *
     * @param message Message to send
     * @param prefix If prefix should be included in message
     */
    public void msgAllPlayers(String message, boolean prefix) {
        String pre = prefix ? lang.prefix : "";
        for (UUID u : players) {
            Player p = Bukkit.getPlayer(u);
            if (p != null)
                Util.scm(p, pre + message);
        }
    }

    /**
     * Send a message to all players/spectators in the game
     * <p>Prefix will be included</p>
     *
     * @param message Message to send
     */
    public void msgAll(String message) {
        msgAll(message, true);
    }

    /**
     * Send a message to all players/spectators in the game
     *
     * @param message Message to send
     * @param prefix If prefix should be included in message
     */
    public void msgAll(String message, boolean prefix) {
        String pre = prefix ? lang.prefix : "";
        List<UUID> allPlayers = new ArrayList<>();
        allPlayers.addAll(players);
        allPlayers.addAll(spectators);
        for (UUID u : allPlayers) {
            Player p = Bukkit.getPlayer(u);
            if (p != null)
                Util.scm(p, pre + message);
        }
    }

    /**
     * Play a sound to all players/spectators in the game
     *
     * @param sound Sound
     * @param volume Volume
     * @param pitch Pitch
     */
    public void soundAll(Sound sound, float volume, float pitch) {
        List<UUID> allPlayers = new ArrayList<>();
        allPlayers.addAll(players);
        allPlayers.addAll(spectators);
        for (UUID u : allPlayers) {
            Player p = Bukkit.getPlayer(u);
            if (p != null)
                p.playSound(p.getLocation(), sound, volume, pitch);
        }
    }

    Location getSpawn(Team team) {
        GameArenaData gameArenaData = game.getGameArenaData();
        Location l = gameArenaData.spawns.get(2 * (Integer.parseInt(team.getId()) - 1));
        if (l == null)
            return null;

        if (containsPlayer(l))
            l = gameArenaData.spawns.get(2 * (Integer.parseInt(team.getId()) - 1) + 1);

        return l;
    }

    Location pickRandomSpawn() {
        GameArenaData gameArenaData = game.getGameArenaData();
        double spawn = getRandomIntegerBetweenRange(gameArenaData.maxPlayers - 1);
        if (containsPlayer(gameArenaData.spawns.get(((int) spawn)))) {
            Collections.shuffle(gameArenaData.spawns);
            for (Location l : gameArenaData.spawns) {
                if (!containsPlayer(l)) {
                    return l;
                }
            }
        }
        return gameArenaData.spawns.get((int) spawn);
    }

    boolean containsPlayer(Location location) {
        if (location == null) return false;

        for (UUID u : players) {
            Player p = Bukkit.getPlayer(u);
            if (p != null && p.getWorld() == location.getWorld() && p.getLocation().distance(location) < 3)
                return true;
        }
        return false;
    }

    boolean vaultCheck(Player player) {
        if (Config.economy) {
            int cost = game.gameArenaData.cost;
            if (Vault.economy.getBalance(player) >= cost) {
                Vault.economy.withdrawPlayer(player, cost);
                return true;
            } else {
                Util.scm(player, lang.prefix + lang.cmd_join_no_money.replace("<cost>", String.valueOf(cost)));
                return false;
            }
        }
        return true;
    }

    /**
     * Add a kill to a player
     *
     * @param player The player to add a kill to
     */
    public void addKill(Player player) {
        this.kills.put(player.getUniqueId(), this.kills.getOrDefault(player.getUniqueId(), 0) + 1);
    }

    /**
     * Add a chest loot to a player
     *
     * @param player The player to add a chest loot to
     */
    public void addChestLoot(Player player) {
        this.chestsLooted.put(player.getUniqueId(), this.chestsLooted.getOrDefault(player.getUniqueId(), 0) + 1);
    }

    /**
     * Join a player to the game
     *
     * @param player Player to join the game
     */
    public void join(Player player) {
        join(player, false);
    }

    /**
     * Join a player to the game
     *
     * @param player  Player to join the game
     * @param command Whether joined using by using a command
     */
    public void join(Player player, boolean command) {
        GameArenaData gameArenaData = game.getGameArenaData();
        Status status = gameArenaData.getStatus();
        if (status != Status.WAITING && status != Status.STOPPED && status != Status.COUNTDOWN && status != Status.READY) {
            if ((status == Status.RUNNING || status == Status.BEGINNING) && Config.spectateEnabled) {
                game.gamePlayerData.spectate(player, true);
            } else {
                Util.scm(player, lang.arena_not_ready);
            }
        } else if (gameArenaData.maxPlayers <= players.size()) {
            Util.scm(player, "&c" + gameArenaData.getName() + " " + lang.game_full);
        } else if (!players.contains(player.getUniqueId())) {
            if (!vaultCheck(player)) {
                return;
            }
            if (game.gameTeamData.getTeamData(player.getUniqueId()).getTeam() == null) {
                if (!Config.practiceMode)
                    Util.scm(player, lang.must_have_team);
                else {
                    TeamGUI teamGUI = new TeamGUI(game);
                    teamGUI.load();
                    teamGUI.open(player);
                }
                return;
            }

            // Call PlayerJoinGameEvent
            PlayerJoinGameEvent event = new PlayerJoinGameEvent(game, player);
            Bukkit.getPluginManager().callEvent(event);
            // If cancelled, stop the player from joining the game
            if (event.isCancelled()) return;

            if (player.isInsideVehicle()) {
                player.leaveVehicle();
            }

            if (players.size() == 0)
                game.getGamePointData().resetAll();

            UUID uuid = player.getUniqueId();
            players.add(uuid);

            Location loc = getSpawn(game.gameTeamData.getTeamData(uuid).getTeam());
            if (loc.getBlock().getRelative(BlockFace.DOWN).getType() == Material.AIR) {
                while (loc.getBlock().getRelative(BlockFace.DOWN).getType() == Material.AIR) {
                    loc.setY(loc.getY() - 1);
                }
            }
            Location previousLocation = player.getLocation();

            for (Player oPlayer : Bukkit.getOnlinePlayers()) {
                if (playerManager.getGame(oPlayer) == null)
                    oPlayer.showPlayer(plugin, player);
            }

            // Teleport async into the arena so it loads a little more smoothly
            PaperLib.teleportAsync(player, loc).thenAccept(a -> {

                PlayerData playerData = new PlayerData(player, game);
                if (command && Config.savePreviousLocation) {
                    playerData.setPreviousLocation(previousLocation);
                }
                playerManager.addPlayerData(playerData);
                gameArenaData.boards.setBoard(player);

                heal(player);
                freeze(player);
                kills.put(player.getUniqueId(), 0);
                chestsLooted.put(player.getUniqueId(), 0);

                if (players.size() == 1 && status == Status.READY)
                    gameArenaData.setStatus(Status.WAITING);
                if (players.size() >= game.gameArenaData.minPlayers && (status == Status.WAITING || status == Status.READY)) {
                    game.startPreGame();
                } else if (status == Status.WAITING || status == Status.COUNTDOWN) {
                    String broadcast = lang.player_joined_game
                            .replace("<arena>", gameArenaData.getName())
                            .replace("<player>", player.getName());
                    msgAll(broadcast);
                }

                kitHelp(player);
                player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1f, 1f);
//                game.getKitManager().setKit(player, game.getKitManager().getKits().get(0));

                game.gameArenaData.aliveCount = lang.players_alive_num.replace("<num>", String.valueOf(players.size()));

                game.gameBlockData.updateLobbyBlock();
                game.gameArenaData.updateBoards();
                game.gameCommandData.runCommands(CommandType.JOIN, player);

                for (Player p : Bukkit.getOnlinePlayers()) {
                    PlayerData data = playerManager.getPlayerData(p);

                    if (data == null || data.getGame() == null) {
                        p.hidePlayer(plugin, player);
                        player.hidePlayer(plugin, p);
                    } else if (data.getGame() == game) {
                        p.showPlayer(plugin, player);
                        player.showPlayer(plugin, p);
                    }
                }

                if (Config.practiceMode)
                    player.getInventory().setItem(8, plugin.getItemStackManager().getQuitGameItem());

                player.teleport(loc);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        player.teleport(loc);
                    }
                }.runTaskLater(plugin, 5L);

                if (game.gameArenaData.status == Status.COUNTDOWN) {
                    Util.scm(player, lang.game_countdown_info);
                }
            });

            soundAll(Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1f, 1f);
        }
    }

    /**
     * Make a player leave the game
     *
     * @param player Player to leave the game
     * @param death  Whether the player has died or not (Genefrally should be false)
     */
    public void leave(Player player, Boolean death) {
        Bukkit.getPluginManager().callEvent(new PlayerLeaveGameEvent(game, player, death));
        UUID uuid = player.getUniqueId();
        players.remove(uuid);
        game.gameArenaData.aliveCount = lang.players_alive_num.replace("<num>", String.valueOf(players.size()));
        if (!death) {
            game.gameArenaData.boards.removeBoard(player);
        }
        unFreeze(player);
        if (death) {
            if (Config.spectateEnabled && Config.spectateOnDeath) {
                spectate(player, false);
//                player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 5, 1);
                player.sendTitle("", Util.getColString(lang.spectator_start_title), 10, 100, 10);
                game.updateAfterDeath(player, true);
                return;
            } else if (game.gameArenaData.getStatus() == Status.RUNNING)
                game.getGameBarData().removePlayer(player);
        }
        heal(player);
        PlayerData playerData = playerManager.getPlayerData(uuid);
        Location previousLocation = playerData.getPreviousLocation();

        playerData.restore(player);
        exit(player, previousLocation);
        playerManager.removePlayerData(player);
        game.updateAfterDeath(player, death);

        TeamData td = game.getGameTeamData().getTeamData(player.getUniqueId());
        if (td.isOnTeam(player.getUniqueId()))
            td.getTeam().leave(player, game, true, false);
    }

    void exit(Player player, @Nullable Location exitLocation) {
        GameArenaData gameArenaData = game.getGameArenaData();

        gameArenaData.boards.removeBoard(player);

        player.setInvulnerable(false);
        if (gameArenaData.getStatus() == Status.RUNNING)
            game.getGameBarData().removePlayer(player);
        Location loc;
        if (exitLocation != null) {
            loc = exitLocation;
        } else if (gameArenaData.exit != null && gameArenaData.exit.getWorld() != null) {
            loc = gameArenaData.exit;
        } else {
            Location worldSpawn = Bukkit.getWorlds().get(0).getSpawnLocation();
            Location bedLocation = player.getBedSpawnLocation();
            loc = bedLocation != null ? bedLocation : worldSpawn;
        }

        PlayerData playerData = playerManager.getData(player);
        if (playerData == null || playerData.isOnline()) {
            PaperLib.teleportAsync(player, loc);
        } else {
            player.teleport(loc);
        }

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (playerManager.getData(p) == null || playerManager.getData(p).getGame() == null) {
                p.showPlayer(plugin, player);
                player.showPlayer(plugin, p);
            } else {
                player.hidePlayer(plugin, p);
                p.hidePlayer(plugin, player);
            }
        }
    }

    /**
     * Put a player into spectator for this game
     *
     * @param spectator The player to spectate
     */
    public void spectate(Player spectator, boolean teleport) {
        UUID uuid = spectator.getUniqueId();
        if (teleport)
            spectator.teleport(game.gameArenaData.getSpawns().get(0));
        if (playerManager.hasPlayerData(uuid)) {
            playerManager.transferPlayerDataToSpectator(uuid);
        } else {
            playerManager.addSpectatorData(new PlayerData(spectator, game));
        }
        this.spectators.add(uuid);
        spectator.getInventory().clear();
        spectator.setGameMode(GameMode.SURVIVAL);
        for (PotionEffect ef : spectator.getActivePotionEffects()) {
            spectator.removePotionEffect(ef.getType());
        }
        spectator.setCollidable(false);
        if (Config.spectateFly)
            spectator.setAllowFlight(true);

        if (Config.spectateHide) {
            for (UUID u : players) {
                Player player = Bukkit.getPlayer(u);
                if (player == null) continue;
                player.hidePlayer(plugin, spectator);
                spectator.showPlayer(plugin, player);
            }
            for (UUID u : spectators) {
                Player player = Bukkit.getPlayer(u);
                if (player == null) continue;
                player.hidePlayer(plugin, spectator);
                spectator.hidePlayer(plugin, player);
            }
        }

        for (Player p : Bukkit.getOnlinePlayers()) {
            Game game = playerManager.getGame(p);

            if (game == null) {
                p.hidePlayer(plugin, spectator);
                spectator.hidePlayer(plugin, p);
            }
        }

        game.getGameBarData().addPlayer(spectator);
        game.gameArenaData.boards.setBoard(spectator);
        spectator.getInventory().setItem(0, plugin.getItemStackManager().getSpectatorCompass());
        if (Config.practiceMode)
            spectator.getInventory().setItem(8, plugin.getItemStackManager().getQuitGameItem());
    }

    /**
     * Remove a player from spectator of this game
     *
     * @param spectator The player to remove
     */
    public void leaveSpectate(Player spectator) {
        UUID uuid = spectator.getUniqueId();
        PlayerData playerData = playerManager.getSpectatorData(uuid);
        if (playerData != null) {
            Location previousLocation = playerData.getPreviousLocation();

            playerData.restore(spectator);
            exit(spectator, previousLocation);
        }
        spectators.remove(spectator.getUniqueId());
        spectator.setCollidable(true);
        if (Config.spectateFly) {
            GameMode mode = spectator.getGameMode();
            if (mode == GameMode.SURVIVAL || mode == GameMode.ADVENTURE)
                spectator.setAllowFlight(false);
        }
//        if (Config.spectateHide)
//            revealPlayer(spectator);
        playerManager.removeSpectatorData(uuid);
    }

    void revealPlayer(Player hidden) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.showPlayer(plugin, hidden);
        }
    }

    public boolean hadPlayed(UUID uuid) {
        return players.contains(uuid) || (spectators.contains(uuid) && (kills.containsKey(uuid) || chestsLooted.containsKey(uuid)));
    }

    // UTIL
    private static double getRandomIntegerBetweenRange(double max) {
        return (int) (Math.random() * ((max - (double) 0) + 1)) + (double) 0;
    }

    public void clearData() {
        kills.clear();
        chestsLooted.clear();
    }
}
