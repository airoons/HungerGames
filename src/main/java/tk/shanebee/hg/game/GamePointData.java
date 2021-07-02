package tk.shanebee.hg.game;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Data class for holding a {@link Game Game's} blocks
 */
public class GamePointData extends Data {

    private Map<Team, Integer> points = new HashMap<>();

    protected GamePointData(Game game) {
        super(game);
    }

    public void addPoints(Team team, int toAdd) {
        if (!points.containsKey(team))
            points.put(team, 0);

        points.put(team, points.get(team) + toAdd);
        points = sortByValue(points);
    }

    public Set<String> getTopPlaces(Player player) {
        Set<String> result = new HashSet<>();

        int i = 0;
        for (Map.Entry<Team, Integer> entry : points.entrySet()) {
            result.add(ChatColor.translateAlternateColorCodes('&', plugin.getLang().scoreboard_line_top_places
                .replace("<place>", String.valueOf(i + 1))
                .replace("<teamname>", entry.getKey().getChatColor() + "Komanda #" + entry.getKey().getId())
                .replace("<points>", String.valueOf(entry.getValue()))
            ));

            i++;

            if (i == 3)
                break;
        }

        if (i < 3) {
            if (i == 0) i = 1;

            while (i != 3) {
                result.add(ChatColor.translateAlternateColorCodes('&', plugin.getLang().scoreboard_line_top_places
                        .replace("<place>", String.valueOf(i))
                        .replace("<teamname>", "???")
                        .replace("<points>", "0")
                ));
                i++;
            }
        }

        result.add(ChatColor.translateAlternateColorCodes('&', plugin.getLang().scoreboard_line_top_places
                .replace("<place>", "10")
                .replace("<teamname>", "Tava komanda")
                .replace("<points>", "0")
        ));

        return result;
    }

    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
        list.sort(Map.Entry.comparingByValue());

        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }
}
