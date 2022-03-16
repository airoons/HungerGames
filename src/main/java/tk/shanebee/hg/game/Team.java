package tk.shanebee.hg.game;

import me.MrGraycat.eGlow.API.EGlowAPI;
import me.MrGraycat.eGlow.API.Enum.EGlowColor;
import me.MrGraycat.eGlow.EGlow;
import me.MrGraycat.eGlow.Manager.Interface.IEGlowPlayer;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import tk.shanebee.hg.HG;
import tk.shanebee.hg.data.Config;
import tk.shanebee.hg.data.PlayerData;
import tk.shanebee.hg.data.TeamData;
import tk.shanebee.hg.managers.PlayerManager;
import tk.shanebee.hg.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a HungerGames team
 */
public class Team {

    private final String id;
    private String oid;
    private final List<UUID> players = new ArrayList<>();
    private final EGlowColor glowColor;

    public Team(String id, EGlowColor glowColor) {
        this.id = id;
        this.glowColor = glowColor;
    }

    /**
     * Resets team
     */
    public void reset(Game game) {
        EGlowAPI eGlowAPI = EGlow.getAPI();

        for (UUID uuid : players) {
            TeamData td = game.getGameTeamData().getTeamData(uuid);
            td.setTeam(null);

            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                IEGlowPlayer ePlayer = eGlowAPI.getEGlowPlayer(p);
                eGlowAPI.resetCustomGlowReceivers(ePlayer);
                eGlowAPI.disableGlow(ePlayer);
            }
        }

        players.clear();
    }

    /**
     * Add a player to this team
     *
     * @param player Player to add
     */
    public void join(Player player, Game game) {
        TeamData td = game.getGameTeamData().getTeamData(player.getUniqueId());
        if (td.getTeam() != null && td.getTeam() == this)
            return;

        td.setTeam(this);

        if (Config.practiceMode) {
            if (players.size() > 0)
                messageMembers(HG.getPlugin().getLang().team_member_joined.replace("<player>", player.getName()));
            Util.scm(player, HG.getPlugin().getLang().joined_team);
        }
        players.add(player.getUniqueId());

        EGlowAPI eGlowAPI = EGlow.getAPI();

        ArrayList<Player> teamOnline = new ArrayList<>();
        for (UUID uuid : players) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                teamOnline.add(p);
                IEGlowPlayer ePlayer = eGlowAPI.getEGlowPlayer(p);
                if (ePlayer != null) {
                    eGlowAPI.addCustomGlowReceiver(ePlayer, player);
                    eGlowAPI.enableGlow(ePlayer, glowColor);
                }
            }
        }

        IEGlowPlayer ePlayer = eGlowAPI.getEGlowPlayer(player);
        eGlowAPI.setCustomGlowReceivers(ePlayer, teamOnline);
        eGlowAPI.enableGlow(ePlayer, glowColor);
    }

    /**
     * Remove a player from this team
     *
     * @param player Player to remove
     */
    public void leave(Player player, Game game, boolean disableGlow, boolean keepInMemory) {
        TeamData td = game.getGameTeamData().getTeamData(player.getUniqueId());
        if (!keepInMemory) {
            td.setTeam(null);
            players.remove(player.getUniqueId());
            if (Config.practiceMode) {
                messageMembers(HG.getPlugin().getLang().team_member_left.replace("<player>", player.getName()));
                Util.scm(player, HG.getPlugin().getLang().left_team);
            }
        }

        EGlowAPI eGlowAPI = EGlow.getAPI();

        for (UUID uuid : players) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                IEGlowPlayer ePlayer = eGlowAPI.getEGlowPlayer(p);
                eGlowAPI.removeCustomGlowReceiver(ePlayer, player);
                eGlowAPI.disableGlow(ePlayer);
                eGlowAPI.enableGlow(ePlayer, glowColor);
                ePlayer = eGlowAPI.getEGlowPlayer(player);
                eGlowAPI.removeCustomGlowReceiver(ePlayer, p);
            }
        }

        if (disableGlow) {
            IEGlowPlayer ePlayer = eGlowAPI.getEGlowPlayer(player);
            eGlowAPI.disableGlow(ePlayer);
        }
    }

    /**
     * Check if a player is on this team
     *
     * @param uuid UUID of player to check
     * @return True if player is on this team
     */
    public boolean isOnTeam(UUID uuid) {
        return (players.contains(uuid));
    }

    /**
     * Get the players on this team
     *
     * @return List of UUIDs of players on this team
     */
    public List<UUID> getPlayers() {
        return players;
    }

    /**
     * Get the id of this team
     *
     * @return Name of team
     */
    public String getId() {
        return id;
    }

    /**
     * Send a message to all members of this team
     *
     * @param message Message to send
     */
    public void messageMembers(String message) {
        for (UUID uuid : this.players) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                Util.scm(player, message);
            }
        }
    }

    @Override
    public String toString() {
        return "Team{players=" + players + '}';
    }

    public ChatColor getChatColor() {
        return Util.getChatColorFromGlow(glowColor);
    }

    public EGlowColor getGlowColor() {
        return glowColor;
    }

    public String getName() {
        return HG.getPlugin().getLang().team_colors.get(glowColor);
    }

    public boolean isAlive(Game game) {
        PlayerManager pm = HG.getPlugin().getPlayerManager();

        for (UUID uuid : this.players) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                PlayerData data = pm.getPlayerData(player);
                if (data != null && data.getGame() == game)
                    return true;
            }
        }

        return false;
    }
}
