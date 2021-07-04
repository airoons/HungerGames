package tk.shanebee.hg.game;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import tk.shanebee.hg.data.Config;
import tk.shanebee.hg.util.Util;

import java.util.*;

/**
 * Data class for holding a {@link Game Game's} team points
 */
public class GamePointData extends Data {

    public Map<Team, Integer> points = new HashMap<>();

    protected GamePointData(Game game) {
        super(game);
        for (Team team : game.plugin.getTeamManager().getTeams()) {
            points.put(team, 0);
        }
    }

    public void addSurvivingPoints(Player player) {
        Team playerTeam = plugin.getTeamManager().getTeamData(player.getUniqueId()).getTeam();

        for (Team team : plugin.getTeamManager().getTeams()) {
            if (team != playerTeam && team.isAlive())
                addPoints(team, Config.pointsPerSurviving);
        }
    }

    public void addGamePoints(Player player, int toAdd) {
        Team team = plugin.getTeamManager().getTeamData(player.getUniqueId()).getTeam();
        if (team == null)
            return;

        addPoints(team, toAdd);
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

    public ArrayList<String> getTopPlaces(Player player) {
        ArrayList<String> result = new ArrayList<>();

        Team playerTeam = plugin.getTeamManager().getTeamData(player.getUniqueId()).getTeam();
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
        for (Team team : game.plugin.getTeamManager().getTeams()) {
            points.put(team, 0);
        }
    };
}
