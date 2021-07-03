package tk.shanebee.hg.game;

import me.MrGraycat.eGlow.API.EGlowAPI;
import me.MrGraycat.eGlow.API.Enum.EGlowColor;
import me.MrGraycat.eGlow.EGlow;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
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

    public Team(String id, EGlowColor glowColor) {
        this.id = id;
        this.glowColor = glowColor;
    }

    /**
     * Add a player to this team
     *
     * @param player Player to add
     */
    public void join(Player player) {
        TeamData td = HG.getPlugin().getTeamManager().getTeamData(player.getUniqueId());
        td.setTeam(this);
        if (players.size() > 0)
            messageMembers(HG.getPlugin().getLang().team_member_joined.replace("<player>", player.getName()));
        players.add(player.getUniqueId());
        Util.scm(player, HG.getPlugin().getLang().joined_team);

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

        Util.resetTabSort(player);
        User user = HG.getPlugin().getLuckPerms().getPlayerAdapter(Player.class).getUser(player);
        user.data().add(Node.builder("tab.sort." + id).build());
        HG.getPlugin().getLuckPerms().getUserManager().saveUser(user);
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
        messageMembers(HG.getPlugin().getLang().team_member_left.replace("<player>", player.getName()));
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

        if (disableGlow) {
            Util.resetTabSort(player);
            eGlowAPI.disableGlow(player);
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
}
