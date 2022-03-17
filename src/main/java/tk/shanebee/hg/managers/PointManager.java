package tk.shanebee.hg.managers;

import tk.shanebee.hg.game.Game;
import tk.shanebee.hg.game.Team;

import java.text.SimpleDateFormat;
import java.util.*;

public class PointManager {

    private static PointManager instance = new PointManager();

    private Map<Game, Long> arenaStartTimes = new HashMap<>();
    private Map<Game, Map<Team, Integer>> points = new HashMap<>();
    private Map<Game, List<String>> history = new HashMap<>();

    public static PointManager get() {
        return instance;
    }

    public void init(Game g, List<Team> teams) {
        points.put(g, new HashMap<>());
        for (Team team : teams) {
            points.get(g).put(team, 1);
        }
        history.put(g, new ArrayList<>());
        arenaStartTimes.put(g, System.currentTimeMillis());
    }

    public Map<Team, Integer> getPoints(Game g) {
        return points.get(g);
    }

    public Map<Team, Integer> getAndClearPoints(Game g) {
        return points.remove(g);
    }

    public List<String> getHistory(Game g) {
        return history.get(g);
    }

    public List<String> getAndClearHistory(Game g) {
        return history.remove(g);
    }

    public long getStartTime(Game g) {
        return arenaStartTimes.getOrDefault(g, 0L);
    }

    public void clearStartTime(Game g) {
        arenaStartTimes.remove(g);
    }

    public String formatSeconds(long ms) {
        Date d = new Date(ms);
        SimpleDateFormat df = new SimpleDateFormat("mm:ss");

        return df.format(d);
    }
}
