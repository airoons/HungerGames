package tk.shanebee.hg.managers;

import lv.side.events.response.ResponseMatchFinishedEvent;
import lv.side.events.response.ResponseMatchStartEvent;
import lv.side.events.response.ResponseAvailableArenasEvent;
import lv.side.objects.EventMatch;
import lv.side.objects.MatchResponse;
import lv.side.objects.SimpleTeam;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import tk.shanebee.hg.HG;
import tk.shanebee.hg.Status;
import tk.shanebee.hg.game.Game;
import tk.shanebee.hg.game.Team;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AssignManager {

    private static AssignManager instance = new AssignManager();
    private static HG plugin = HG.getPlugin();

    public Map<Game, EventMatch> assignedMatches = new HashMap<>();
    public Map<Game, Map<Team, SimpleTeam>> assignedTeams = new HashMap<>();
    public Map<String, Game> assignedPlayers = new HashMap<>();
    public Map<String, Team> playerTeams = new HashMap<>();
    public List<Game> resetRequests = new ArrayList<>();

    public static AssignManager get() {
        return instance;
    }

    public boolean isAssigned(Game arena) {
        return assignedMatches.containsKey(arena);
    }

    public void playerJoin(Player player) {
        if (player.hasPermission("event.spectate"))
            return;

        if (!assignedPlayers.containsKey(player.getName())) {
            player.kickPlayer("Notika kļūda pievienojot tevi spēlei, lūdzu sazinies ar turnīra organizatoriem!");
            return;
        }

        Game g = assignedPlayers.get(player.getName());
        Team team = null;
        for (Map.Entry<Team, SimpleTeam> entry : assignedTeams.get(g).entrySet()) {
            if (entry.getValue().getMembers().contains(player.getName())) {
                team = entry.getKey();
                break;
            }
        }

        if (team == null) {
            player.kickPlayer("Notika kļūda pievienojot tevi spēlei (nevarēja saistīt komandu), lūdzu sazinies ar turnīra organizatoriem!");
            return;
        }

        team.join(player, g);
        playerTeams.put(player.getName(), team);
        g.getGamePlayerData().join(player);
    }

    public void updateAvailableArenas() {
        List<String> arenas = new ArrayList<>();

        for (Game g : plugin.getGames()) {
            if ((g.getGameArenaData().getStatus() == Status.READY || g.getGameArenaData().getStatus() == Status.WAITING) && !AssignManager.get().isAssigned(g))
                arenas.add(g.getGameArenaData().getName());
        }

        Bukkit.getPluginManager().callEvent(new ResponseAvailableArenasEvent(arenas));
    }

    public void callMatchStartEvent(int count, int code) {
        Bukkit.getPluginManager().callEvent(new ResponseMatchStartEvent(new MatchResponse(count, code)));
    }

    public void callMatchFinishedEvent(EventMatch match) {
        Bukkit.getPluginManager().callEvent(new ResponseMatchFinishedEvent(match));
    }
}
