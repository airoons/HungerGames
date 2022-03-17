package tk.shanebee.hg.game;

import lv.side.objects.SimpleTeam;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import tk.shanebee.hg.PointType;
import tk.shanebee.hg.data.Config;
import tk.shanebee.hg.managers.AssignManager;
import tk.shanebee.hg.managers.PointManager;

import java.util.*;
import java.util.List;

/**
 * Data class for holding a {@link Game Game's} team points
 */
public class GamePointData extends Data {

    public Map<Team, Integer> points = new HashMap<>();
    public Map<Team, List<Team>> kills = new HashMap<>();
    public Map<Team, List<String>> debugLog = new HashMap<>();
    public int placement = 0;
    public int addedPlace;

    protected GamePointData(Game game) {
        super(game);
        for (Team team : game.gameTeamData.getTeams()) {
            points.put(team, 0);
            kills.put(team, new ArrayList<>());
            debugLog.put(team, new ArrayList<>());
        }
    }

    public void addGamePoints(Team team, PointType type) {
        int toAdd = 0;
        PointManager pm = PointManager.get();
        String timeNow = pm.formatSeconds(System.currentTimeMillis() - pm.getStartTime(game));

        if (type == PointType.KILL) {
            toAdd = Config.pointsPerKill;

            debugLog.get(team).add("killed a player: " + toAdd);
            pm.getHistory(game).add(
                    "[" + timeNow + " KILL punkti] " + team.getName() + " +" + toAdd
            );
            pm.getPoints(game).put(team, pm.getPoints(game).get(team) + toAdd);

        } else if (type == PointType.TEAM_KILL) {
            toAdd = Config.pointsPerKill + Config.pointsPerTeamBonus;

            debugLog.get(team).add("killed a player (+ full team kill bonus): " + toAdd);
            pm.getHistory(game).add(
                    "[" + timeNow + " TEAM_KILL punkti] " + team.getName() + " +" + toAdd
            );
            pm.getPoints(game).put(team, pm.getPoints(game).get(team) + toAdd);
        } else if (type == PointType.PLACEMENT) {
            toAdd = Config.pointsPerPlacement.get(placement) - addedPlace;
            addedPlace += toAdd;

            int teamsAlive = 0;
            for (Team iTeam : points.keySet()) {
                if (iTeam != team && !iTeam.isAlive(game))
                    continue;

                if (iTeam != team && iTeam.isAlive(game))
                    teamsAlive++;
                addPoints(iTeam, toAdd);
                debugLog.get(iTeam).add("placement: " + toAdd);

                if (iTeam == team) {
                    pm.getHistory(game).add(
                            "[" + timeNow + " PLACEMENT punkti] " + team.getName() + " +" + Config.pointsPerPlacement.get(placement)
                    );
                    pm.getPoints(game).put(team, pm.getPoints(game).get(team) + Config.pointsPerPlacement.get(placement));
                }
            }

            if (!Config.practiceMode && AssignManager.get().assignedTeams.containsKey(game)) {
                SimpleTeam simpleTeam = AssignManager.get().assignedTeams.get(game).get(team);
                if (simpleTeam != null)
                    AssignManager.get().assignedMatches.get(game).getAliveMs().put(simpleTeam.getId(), (int) (System.currentTimeMillis() - pm.getStartTime(game)));
            }

            placement = teamsAlive;
            return;
        }

        addPoints(team, toAdd);
    }

    public void printDebugLog() {
        plugin.getLogger().info("---- Point history: -----");
        for (Map.Entry<Team, List<String>> entry : debugLog.entrySet()) {
            for (String log : entry.getValue())
                plugin.getLogger().info("[" + entry.getKey().getName() + "] " + log);
        }
    }

    public void addPoints(Team team, int toAdd) {
        if (!points.containsKey(team))
            points.put(team, 0);

        points.put(team, points.get(team) + toAdd);
        points = sortByValue(points);
    }

    public Team getWinnerTeam() {
        points = sortByValue(points);

        for (Map.Entry<Team, Integer> entry : points.entrySet()) {
            return entry.getKey();
        }

        return null;
    }

    public ArrayList<String> getAll() {
        ArrayList<String> result = new ArrayList<>();

        int i = 1;

        for (Map.Entry<Team, Integer> entry : points.entrySet()) {
            if (entry.getValue() == 0 && !Config.practiceMode && (PointManager.get().getPoints(game) == null || !PointManager.get().getPoints(game).containsKey(entry.getKey())))
                continue;
            result.add(getAdvTeamPointsFormatted(entry.getKey(), i, entry.getValue(), !Config.practiceMode));
            i++;
        }

        return result;
    }

    public ArrayList<String> getAll(Player player) {
        ArrayList<String> result = new ArrayList<>();

        int i = 1;

        for (Map.Entry<Team, Integer> entry : points.entrySet()) {
            if (entry.getValue() == 0 && !Config.practiceMode && (PointManager.get().getPoints(game) == null || !PointManager.get().getPoints(game).containsKey(entry.getKey())))
                continue;
            result.add(getAdvTeamPointsFormatted(entry.getKey(), i, entry.getValue(), !Config.practiceMode));
            i++;
        }

        return result;
    }

    public ArrayList<String> getTopPlaces(Player player) {
        ArrayList<String> result = new ArrayList<>();

        Team playerTeam = game.gameTeamData.getTeamData(player.getUniqueId()).getTeam();
        int playerPos = getPlace(playerTeam);

        int i = 1;
        int added = 0;

        for (Map.Entry<Team, Integer> entry : points.entrySet()) {
            if (i == 1 || (playerPos == i) ||
                (playerPos < 12 && (playerPos < i || playerPos - 1 == i)) ||
                (playerPos == 12 && i > 9)) {
                result.add(getTeamPointsFormatted(entry.getKey(), i, entry.getValue(), entry.getKey() == playerTeam));
                added++;
            }

            i++;
            if (added == 4)
                break;
        }

        return result;
    }

    public int getPlace(Team team) {
        int i = 1;
        for (Map.Entry<Team, Integer> entry : points.entrySet()) {
            if (entry.getKey() == team)
                return i;
            i++;
        }

        return -1;
    }

    private String getAdvTeamPointsFormatted(Team team, int place, int points, boolean added) {
        if (team == null)
            return "";

        return ChatColor.translateAlternateColorCodes('&', plugin.getLang().scoreboard_line_top_places
                .replace("<place>", String.valueOf(place))
                .replace("<teamname>", team.getChatColor() + lang.team_colors.get(team.getGlowColor()))
                .replace("<points>", String.valueOf(points)) + (added ? " &7&o" + AssignManager.get().assignedTeams.get(game).get(team).getName() : "")
        );
    }

    private String getTeamPointsFormatted(Team team, int place, int points, boolean isPlayerTeam) {
        if (team == null)
            return "";

        return ChatColor.translateAlternateColorCodes('&', plugin.getLang().scoreboard_line_top_places
                .replace("<place>", String.valueOf(place))
                .replace("<teamname>", team.getChatColor() + (isPlayerTeam ? "&l" : "") + lang.team_colors.get(team.getGlowColor()))
                .replace("<points>", String.valueOf(points))
        );
    }

    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
        list.sort(Map.Entry.comparingByValue());
        Collections.reverse(list);

        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }

    public void resetAll() {
        for (Team team : game.gameTeamData.getTeams()) {
            points.put(team, 0);
            kills.put(team, new ArrayList<>());
            debugLog.put(team, new ArrayList<>());
        }
        addedPlace = 0;
        placement = 0;
    }

    public boolean hasKilledBefore(Team team, Team toCheck) {
        return kills.get(team).contains(toCheck);
    }

    public void setPlacement(int placement) {
        this.placement = placement;
    }
}
