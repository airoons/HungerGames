package tk.shanebee.hg.game;

import me.MrGraycat.eGlow.API.EGlowAPI;
import me.MrGraycat.eGlow.API.Enum.EGlowColor;
import me.MrGraycat.eGlow.EGlow;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import tk.shanebee.hg.HG;
import tk.shanebee.hg.data.TeamData;
import tk.shanebee.hg.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a HungerGames team
 */
public class Team {

    private final String id;
    private final List<UUID> players = new ArrayList<>();
    private final EGlowColor glowColor;
//    private final org.bukkit.scoreboard.Team bukkitTeam;

    public Team(Player leader, String id, EGlowColor glowColor) {
        HG plugin = HG.getPlugin();
        this.id = id;
        TeamData td = plugin.getTeamManager().getTeamData(leader.getUniqueId());
        players.add(leader.getUniqueId());
        td.setTeam(this);

        EGlowAPI eGlowAPI = EGlow.getAPI();
        this.glowColor = glowColor;
        eGlowAPI.enableGlow(leader, glowColor);
        eGlowAPI.addCustomGlowReceiver(leader, leader);

        // Board/McTeam stuff
//        bukkitTeam = game.gameArenaData.getBoard().registerTeam(id+);
//        bukkitTeam.addEntry(leader.getName());
    }

    /**
     * Add a player to this team
     *
     * @param player Player to add
     */
    public void join(Player player) {
        TeamData td = HG.getPlugin().getTeamManager().getTeamData(player.getUniqueId());
        td.setTeam(this);
        players.add(player.getUniqueId());
        Util.scm(player, HG.getPlugin().getLang().joined_team);
//        bukkitTeam.addEntry(player.getName());

        EGlowAPI eGlowAPI = EGlow.getAPI();

        ArrayList<Player> teamOnline = new ArrayList<>();
        for (UUID uuid : players) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                teamOnline.add(p);
                eGlowAPI.addCustomGlowReceiver(p, player);
                eGlowAPI.enableGlow(p, glowColor);
            }
        }

        eGlowAPI.setCustomGlowReceivers(player, teamOnline);
        eGlowAPI.enableGlow(player, glowColor);
    }

    /**
     * Remove a player from this team
     *
     * @param player Player to remove
     */
    public void leave(Player player, boolean disableGlow) {
        TeamData td = HG.getPlugin().getTeamManager().getTeamData(player.getUniqueId());
        td.setTeam(null);
        players.remove(player.getUniqueId());
        Util.scm(player, HG.getPlugin().getLang().left_team);

        EGlowAPI eGlowAPI = EGlow.getAPI();

        for (UUID uuid : players) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                eGlowAPI.removeCustomGlowReceiver(p, player);
                eGlowAPI.disableGlow(p);
                eGlowAPI.enableGlow(p, glowColor);
                eGlowAPI.removeCustomGlowReceiver(player, p);
            }
        }

        if (disableGlow)
            eGlowAPI.disableGlow(player);
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
}
