package tk.shanebee.hg.listeners;

import lv.side.events.RequestArenaMatchEvent;
import lv.side.events.RequestAvailableArenasEvent;
import lv.side.events.RequestMatchResetEvent;
import lv.side.events.response.ResponseArenaMatchEvent;
import lv.side.events.response.ResponseMatchResetEvent;
import lv.side.objects.MatchResponse;
import lv.side.objects.SimpleTeam;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import tk.shanebee.hg.HG;
import tk.shanebee.hg.Status;
import tk.shanebee.hg.events.GameEndEvent;
import tk.shanebee.hg.game.Game;
import tk.shanebee.hg.game.Team;
import tk.shanebee.hg.managers.AssignManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class MatchListeners implements Listener {

    private static HG plugin = HG.getPlugin();

    @EventHandler
    public void onAvailableArenasRequest(RequestAvailableArenasEvent event) {
        AssignManager.get().updateAvailableArenas();
    }

    @EventHandler
    public void onArenaMatchRequest(RequestArenaMatchEvent event) {
        Game g = plugin.getManager().getGame(event.getMatch().getArena());
        if (g == null || AssignManager.get().assignedMatches.containsKey(g) ||
                (g.getGameArenaData().getStatus() != Status.READY && g.getGameArenaData().getStatus() != Status.WAITING)) {
            MatchResponse response = new MatchResponse(event.getMatch().getCount(), 0);
            Bukkit.getPluginManager().callEvent(new ResponseArenaMatchEvent(response));
            return;
        }

        AssignManager.get().resetRequests.remove(g);

        List<Team> enabledTeams = new ArrayList<>(g.getGameTeamData().getTeams());
        Random rand = new Random();
        AssignManager.get().assignedTeams.put(g, new HashMap<>());

        for (SimpleTeam team : event.getMatch().getTeams().keySet()) {
            AssignManager.get().assignedTeams.get(g).put(enabledTeams.remove(rand.nextInt(enabledTeams.size())), team);
            for (String member : team.getMembers()) {
                AssignManager.get().assignedPlayers.put(member, g);
            }
        }

        AssignManager.get().assignedMatches.put(g, event.getMatch());

        MatchResponse response = new MatchResponse(event.getMatch().getCount(), 1);
        Bukkit.getPluginManager().callEvent(new ResponseArenaMatchEvent(response));
    }

    @EventHandler
    public void onMatchReset(RequestMatchResetEvent event) {
        for (Game g : plugin.getGames()) {
            if (g.getGameArenaData().getName().equalsIgnoreCase(event.getMatch().getArena())) {
                AssignManager.get().assignedTeams.remove(g);
                AssignManager.get().assignedMatches.remove(g);
                for (SimpleTeam team : event.getMatch().getTeams().keySet()) {
                    for (String member : team.getMembers()) {
                        AssignManager.get().assignedPlayers.remove(member);
                    }
                }
                if (g.getGameArenaData().getStatus() != Status.READY && g.getGameArenaData().getStatus() != Status.WAITING) {
                    AssignManager.get().resetRequests.add(g);
                    g.stop(false);
                } else {
                    AssignManager.get().updateAvailableArenas();
                }
                Bukkit.getPluginManager().callEvent(new ResponseMatchResetEvent(new MatchResponse(event.getMatch().getCount(), 1)));
                return;
            }
        }


        Bukkit.getPluginManager().callEvent(new ResponseMatchResetEvent(new MatchResponse(event.getMatch().getCount(), 0)));
    }

    @EventHandler
    public void onArenaEnd(GameEndEvent event) {
        new BukkitRunnable() {
            @Override
            public void run() {
                AssignManager.get().updateAvailableArenas();
            }
        }.runTaskLater(plugin, 40L);
    }
}
