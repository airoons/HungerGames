package tk.shanebee.hg.data;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.Nullable;
import tk.shanebee.hg.HG;
import tk.shanebee.hg.game.Game;
import tk.shanebee.hg.game.Team;
import tk.shanebee.hg.util.Util;

import java.util.Arrays;
import java.util.UUID;

/**
 * Player data object for holding pre-game player info
 */
@SuppressWarnings("WeakerAccess")
public class PlayerData implements Cloneable {

    //Pregame data
    private final ItemStack[] inv;
    private final ItemStack[] equip;
    private final ItemStack[] extra;
    private final int expL;
    private final float expP;
    private final double health;
    private final int food;
    private final float saturation;
    private final GameMode mode;
    private final UUID uuid;
    private final Scoreboard scoreboard;
    private Location previousLocation = null;
    private boolean online;

    //InGame data
    private final Game game;

    /**
     * New player pre-game data file
     *
     * @param player Player to save
     * @param game   Game they will be entering
     */
    public PlayerData(Player player, Game game) {
        this.game = game;
        this.uuid = player.getUniqueId();
        inv = player.getInventory().getStorageContents();
        equip = player.getInventory().getArmorContents();
        extra = player.getInventory().getExtraContents();
        expL = player.getLevel();
        expP = player.getExp();
        mode = player.getGameMode();
        food = player.getFoodLevel();
        saturation = player.getSaturation();
        health = player.getHealth();
        Util.clearInv(player);
        player.setLevel(0);
        player.setExp(0);
        scoreboard = player.getScoreboard();
        online = true;
    }

    /**
     * Restore a player's saved data
     *
     * @param player Player to restore data to
     */
    public void restore(Player player) {
        if (player == null) return;
        Util.clearInv(player);
        player.setWalkSpeed(0.2f);
        player.setLevel(expL);
        player.setExp(expP);
        player.setFoodLevel(food);
        player.setSaturation(saturation);
        PlayerInventory inventory = player.getInventory();
        inventory.setStorageContents(inv);
        inventory.setArmorContents(equip);
        inventory.setExtraContents(extra);
        player.setGameMode(mode);
        player.updateInventory();
        player.setInvulnerable(false);
        restoreHealth(player);
        player.setScoreboard(scoreboard);
    }

    // Restores later if player has an item in their inventory which changes their max health value
    @SuppressWarnings("ConstantConditions")
    private void restoreHealth(Player player) {
        double att = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        if (health > att) {
            Bukkit.getScheduler().runTaskLater(HG.getPlugin(), () -> player.setHealth(health), 10);
        } else {
            player.setHealth(health);
        }
    }

    /**
     * Get the game of this player data
     *
     * @return The game of this player data
     */
    public Game getGame() {
        return game;
    }

    /**
     * Get the gamemode of this player data
     *
     * @return Gamemode of this player data
     */
    public GameMode getGameMode() {
        return this.mode;
    }

    /**
     * Get the UUID belonging to this player data
     *
     * @return UUID belonging to this player data
     */
    public UUID getUuid() {
        return this.uuid;
    }

    /**
     * Set the previous location of the player
     *
     * @param previousLocation Location player was at before entering arena
     */
    public void setPreviousLocation(Location previousLocation) {
        this.previousLocation = previousLocation;
    }

    /**
     * Get the previous location of the player
     *
     * @return Location player was at before entering arena
     */
    @Nullable
    public Location getPreviousLocation() {
        if (previousLocation != null) {
            return previousLocation.clone();
        } else {
            return null;
        }
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    /**
     * Get the {@link Player Bukkit Player} belonging to this player data
     *
     * @return Player belonging to this player data
     */
    public Player getBukkitPlayer() {
        return Bukkit.getPlayer(this.uuid);
    }

    @Override
    public String toString() {
        return "PlayerData{" +
                "inv=" + Arrays.toString(inv) +
                ", equip=" + Arrays.toString(equip) +
                ", expLevel=" + expL +
                ", expPoints=" + expP +
                ", health=" + health +
                ", food=" + food +
                ", saturation=" + saturation +
                ", mode=" + mode +
                ", uuid=" + uuid +
                ", game=" + game +
                '}';
    }

    @Override
    public PlayerData clone() {
        try {
            return (PlayerData) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
    }

}
