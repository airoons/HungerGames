package tk.shanebee.hg.managers;

import lv.side.objects.SimpleTeam;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import tk.shanebee.hg.HG;
import tk.shanebee.hg.data.Config;
import tk.shanebee.hg.data.Language;
import tk.shanebee.hg.data.Leaderboard;
import tk.shanebee.hg.data.TeamData;
import tk.shanebee.hg.game.Game;
import tk.shanebee.hg.game.Team;

/**
 * Internal placeholder class
 */
public class Placeholders extends PlaceholderExpansion {

    private HG plugin;
    private Leaderboard leaderboard;
    private Language lang;

    public Placeholders(HG plugin) {
        this.plugin = plugin;
        this.leaderboard = plugin.getLeaderboard();
        this.lang = plugin.getLang();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String getIdentifier() {
        return "hungergames";
    }

    @Override
    public String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public String onRequest(OfflinePlayer player, String identifier) {
        if (identifier.equalsIgnoreCase("team_prefix")) {
            if (!player.isOnline())
                return "";
            Player onlinePlayer = player.getPlayer();

            return getTeamPrefixFormatted(onlinePlayer);
        }

        if (identifier.equalsIgnoreCase("team_color")) {
            if (!player.isOnline())
                return "";
            Player onlinePlayer = player.getPlayer();

            return getTeamColor(onlinePlayer);
        }

        if (identifier.startsWith("lb_player_")) {
            int leader = Integer.parseInt(identifier.replace("lb_player_", ""));
            if (leaderboard.getStatsPlayers(Leaderboard.Stats.WINS).size() >= leader)
                return leaderboard.getStatsPlayers(Leaderboard.Stats.WINS).get(leader - 1);
            else
                return lang.lb_blank_space;
        }
        if (identifier.startsWith("lb_score_")) {
            int leader = (Integer.parseInt(identifier.replace("lb_score_", "")));
            if (leaderboard.getStatsScores(Leaderboard.Stats.WINS).size() >= leader)
                return leaderboard.getStatsScores(Leaderboard.Stats.WINS).get(leader - 1);
            else
                return lang.lb_blank_space;

        }
        if (identifier.startsWith("lb_combined_")) {
            int leader = (Integer.parseInt(identifier.replace("lb_combined_", "")));
            if (leaderboard.getStatsPlayers(Leaderboard.Stats.WINS).size() >= leader)
                return leaderboard.getStatsPlayers(Leaderboard.Stats.WINS).get(leader - 1) + lang.lb_combined_separator +
                        leaderboard.getStatsScores(Leaderboard.Stats.WINS).get(leader - 1);
            else
                return lang.lb_blank_space + lang.lb_combined_separator + lang.lb_blank_space;
        }
        if (identifier.equalsIgnoreCase("lb_player")) {
            return String.valueOf(leaderboard.getStat(player.getUniqueId(), Leaderboard.Stats.WINS));
        }

        if (identifier.equalsIgnoreCase("tab_team")) {
            if (!Config.practiceMode) {
                Game g = AssignManager.get().assignedPlayers.get(player.getName());
                if (g == null)
                    return "";

                if (!AssignManager.get().assignedTeams.containsKey(g))
                    return "";

                Team team = AssignManager.get().playerTeams.get(player.getName());
                if (team == null)
                    return "";

                SimpleTeam simpleTeam = AssignManager.get().assignedTeams.get(g).get(team);
                if (simpleTeam == null)
                    return "";

                return " &7&o" + simpleTeam.getName();
            } else {
                return "";
            }
        }

        String[] id = identifier.split("_");
        switch (id[0]) {
            case "lb":
                switch (id[1]) {
                    case "wins":
                    case "kills":
                    case "deaths":
                    case "games":
                        if (id[2].equalsIgnoreCase("p"))
                            return getStatPlayers(identifier);
                        else if (id[2].equalsIgnoreCase("s"))
                            return getStatScores(identifier);
                        else if (id[2].equalsIgnoreCase("c"))
                            return getStatPlayers(identifier) + " : " + getStatScores(identifier);
                        else if (id[2].equalsIgnoreCase("player"))
                            return getStatsPlayer(identifier, player);
                }
            case "status":
                return HG.getPlugin().getManager().getGame(id[1]).getGameArenaData().getStatus().getName();
            case "cost":
                return String.valueOf(HG.getPlugin().getManager().getGame(id[1]).getGameArenaData().getCost());
            case "playerscurrent":
                return String.valueOf(HG.getPlugin().getManager().getGame(id[1]).getGamePlayerData().getPlayers().size());
            case "playersmax":
                return String.valueOf(HG.getPlugin().getManager().getGame(id[1]).getGameArenaData().getMaxPlayers());
            case "playersmin":
                return String.valueOf(HG.getPlugin().getManager().getGame(id[1]).getGameArenaData().getMinPlayers());
        }
        return null;
    }

    private String getStatsPlayer(String identifier, OfflinePlayer player) {
        String[] ind = identifier.split("_");
        Leaderboard.Stats stat = Leaderboard.Stats.valueOf(ind[1].toUpperCase());
        return String.valueOf(leaderboard.getStat(player.getUniqueId(), stat));
    }

    private String getStatPlayers(String identifier) {
        String[] ind = identifier.split("_");
        Leaderboard.Stats stat = Leaderboard.Stats.valueOf(ind[1].toUpperCase());
        int leader = (Integer.parseInt(ind[3]));
        if (leaderboard.getStatsPlayers(stat).size() >= leader) {
            return leaderboard.getStatsPlayers(stat).get(leader - 1);
        } else {
            return lang.lb_blank_space;
        }
    }

    private String getStatScores(String identifier) {
        String[] ind = identifier.split("_");
        Leaderboard.Stats stat = Leaderboard.Stats.valueOf(ind[1].toUpperCase());
        int leader = (Integer.parseInt(ind[3]));
        if (leaderboard.getStatsScores(stat).size() >= leader) {
            return leaderboard.getStatsScores(stat).get(leader - 1);
        } else {
            return lang.lb_blank_space;
        }
    }

    public static String getTeamColor(Player player) {
        Game game = HG.getPlugin().getPlayerManager().getGame(player);

        String nickColor = (HG.getPlugin().getPlayerManager().hasPlayerData(player)) ? "&f" : "&7";

        if (game != null) {
            TeamData td = game.getGameTeamData().getTeamData(player.getUniqueId());
            if (td.isOnTeam(player.getUniqueId()))
                return String.valueOf(td.getTeam().getChatColor());
        }

        return nickColor;
    }

    public static String getTeamPrefixFormatted(Player player) {
        Game game = HG.getPlugin().getPlayerManager().getGame(player);

        String nickColor = (HG.getPlugin().getPlayerManager().hasPlayerData(player)) ? "&f" : "&7";
        if (player.hasPermission("hg.admin.chat"))
            nickColor = "&c";

        if (game != null) {
            TeamData td = game.getGameTeamData().getTeamData(player.getUniqueId());
            if (td.isOnTeam(player.getUniqueId()))
                return td.getTeam().getChatColor() + "&l" + HG.getPlugin().getLang().team_colors.get(td.getTeam().getGlowColor()) + nickColor + " ";
        }

        return nickColor;
    }

    public static String getTeamColorFormatted(Player player) {
        Game game = HG.getPlugin().getPlayerManager().getGame(player);

        String nickColor = (HG.getPlugin().getPlayerManager().hasPlayerData(player)) ? "&f" : "&7";
        if (player.hasPermission("hg.admin.chat"))
            nickColor = "&c";

        if (game != null) {
            TeamData td = game.getGameTeamData().getTeamData(player.getUniqueId());
            return String.valueOf(td.getTeam().getChatColor());
        }

        return nickColor;
    }
}
