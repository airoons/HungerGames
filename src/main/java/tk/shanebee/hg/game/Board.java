package tk.shanebee.hg.game;

import fr.minuskube.netherboard.Netherboard;
import fr.minuskube.netherboard.bukkit.BPlayerBoard;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.scoreboard.Team.Option;
import org.bukkit.scoreboard.Team.OptionStatus;
import tk.shanebee.hg.HG;
import tk.shanebee.hg.Status;
import tk.shanebee.hg.data.Config;
import tk.shanebee.hg.managers.PlayerManager;
import tk.shanebee.hg.util.Util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a team based scoreboard for a game
 */
public class Board {

    private static final ChatColor[] COLORS;

    static {
        COLORS = new ChatColor[]{ChatColor.AQUA, ChatColor.GREEN, ChatColor.YELLOW, ChatColor.RED, ChatColor.LIGHT_PURPLE, ChatColor.GOLD};
    }

    private final Game game;
    private final HG plugin;
    private final Scoreboard scoreboard;
    private final Team[] lines = new Team[15];
    private final Team team;
    private final String[] entries = new String[]{"&1&r", "&2&r", "&3&r", "&4&r", "&5&r", "&6&r", "&7&r", "&8&r", "&9&r", "&0&r", "&a&r", "&b&r", "&c&r", "&d&r", "&e&r"};
    private Map<UUID, BPlayerBoard> boards = new HashMap<>();
    private final PlayerManager playerManager;

    @SuppressWarnings("ConstantConditions")
    public Board(Game game) {
        this.game = game;
        this.plugin = game.plugin;
        this.playerManager = plugin.getPlayerManager();
        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

        for (int i = 0; i < 15; i++) {
            lines[i] = scoreboard.registerNewTeam("line" + (i + 1));
        }

        for (int i = 0; i < 15; i++) {
            lines[i].addEntry(Util.getColString(entries[i]));
        }
        team = scoreboard.registerNewTeam("game-team");

        if (Config.hideNametags) {
            team.setOption(Option.NAME_TAG_VISIBILITY, OptionStatus.NEVER);
        }
    }

    Team registerTeam(String name) {
        Team team = scoreboard.registerNewTeam(name);
        String prefix = Util.getColString(plugin.getLang().team_prefix.replace("<name>", name) + " ");
        team.setPrefix(prefix);
        String suffix = Util.getColString(" " + plugin.getLang().team_suffix.replace("<name>", name));
        team.setSuffix(suffix);
        team.setColor(COLORS[plugin.getTeamManager().getTeamSize() % COLORS.length]);
        if (Config.hideNametags && Config.team_showTeamNames) {
            team.setOption(Option.NAME_TAG_VISIBILITY, OptionStatus.FOR_OTHER_TEAMS);
        }
        team.setAllowFriendlyFire(Config.team_friendly_fire);
        team.setCanSeeFriendlyInvisibles(Config.team_see_invis);
        return team;
    }

    /**
     * Add a player to this scoreboard
     *
     * @param player Player to add
     */
    public void setBoard(Player player) {
        player.setScoreboard(scoreboard);
        team.addEntry(player.getName());

        registerBoard(player);
    }

    public void removeBoard(Player player) {
        if (boards.containsKey(player.getUniqueId())) {
            boards.get(player.getUniqueId()).delete();
            boards.remove(player.getUniqueId());
        }
    }

    private void registerBoard(Player player) {
        BPlayerBoard board = Netherboard.instance().createBoard(player, Util.getColString(plugin.getLang().scoreboard_title));

        fullUpdate(board, player);

        boards.put(player.getUniqueId(), board);
    }

    public void fullUpdate(Player player) {
        BPlayerBoard board = boards.get(player.getUniqueId());
        if (board == null)
            return;

        fullUpdate(board, player);
    }

    public void fullUpdate(BPlayerBoard board, Player player) {
        int i = 12;
        if (game.getGameArenaData().getStatus() != Status.RUNNING)
            i -= 6;
        if (playerManager.hasSpectatorData(player))
            i -= 4;

        board.set(Util.getColString(plugin.getLang().scoreboard_line_empty), i--);

        // alive
        board.set(Util.getColString(plugin.getLang().scoreboard_line_1 + game.getGameArenaData().aliveCount), i--);
        // remaining time
        board.set(Util.getColString(plugin.getLang().scoreboard_line_2 + game.getGameArenaData().timeLeft), i--);
        board.set(Util.getColString(plugin.getLang().scoreboard_line_empty + " "), i--);

        if (game.getGameArenaData().getStatus() == Status.RUNNING) {
            board.set(Util.getColString(plugin.getLang().scoreboard_line_top), i--);

            for (int place = 1; place <= 4; place++) {
                board.set(Util.getColString(plugin.getLang().scoreboard_line_top_places
                        .replace("<place>", String.valueOf(place))
                        .replace("<teamname>", "???")
                        .replace("<points>", String.valueOf(0))), i--);
            }

            board.set(Util.getColString(plugin.getLang().scoreboard_line_empty + "  "), i--);
        }

        if (!playerManager.hasSpectatorData(player)) {
            // kills
            board.set(Util.getColString(plugin.getLang().scoreboard_line_self_1 + game.gamePlayerData.kills.get(player.getUniqueId())), i--);
            // chests looted
            board.set(Util.getColString(plugin.getLang().scoreboard_line_self_2 + game.gamePlayerData.chestsLooted.get(player.getUniqueId())), i--);
            board.set(Util.getColString(plugin.getLang().scoreboard_line_empty + "    "), i);
        }
    }

    @Override
    public String toString() {
        return "Board{game=" + game + '}';
    }

    public void updateBoard() {
        for (Map.Entry<UUID, BPlayerBoard> entry : boards.entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player == null || !player.isOnline())
                continue;

            int i = 11;
            if (game.getGameArenaData().getStatus() != Status.RUNNING)
                i -= 6;
            if (playerManager.hasSpectatorData(player))
                i -= 4;

            // alive
            entry.getValue().set(Util.getColString(plugin.getLang().scoreboard_line_1 + game.getGameArenaData().aliveCount), i--);
            // remaining time
            entry.getValue().set(Util.getColString(plugin.getLang().scoreboard_line_2 + game.getGameArenaData().timeLeft), i);

            if (game.getGameArenaData().getStatus() != Status.RUNNING)
                continue;

            i -= 3;
            for (String placeEntry : game.gamePointData.getTopPlaces(player)) {
                entry.getValue().set(placeEntry, i--);
            }

            if (playerManager.hasSpectatorData(player))
                continue;

            // kills
            entry.getValue().set(Util.getColString(plugin.getLang().scoreboard_line_self_1 + game.gamePlayerData.kills.get(player.getUniqueId())), 2);
            // chests looted
            entry.getValue().set(Util.getColString(plugin.getLang().scoreboard_line_self_2 + game.gamePlayerData.chestsLooted.get(player.getUniqueId())), 1);
        }
    }
}
