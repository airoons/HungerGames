package tk.shanebee.hg.game;

import fr.mrmicky.fastboard.FastBoard;
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

import java.util.*;

/**
 * Represents a team based scoreboard for a game
 */
public class Boards {

    private final Game game;
    private final HG plugin;
    private final Scoreboard scoreboard;
    private final Team[] lines = new Team[15];
    private final Team team;
    private final String[] entries = new String[]{"&1&r", "&2&r", "&3&r", "&4&r", "&5&r", "&6&r", "&7&r", "&8&r", "&9&r", "&0&r", "&a&r", "&b&r", "&c&r", "&d&r", "&e&r"};
    private Map<UUID, FastBoard> boards = new HashMap<>();
    private final PlayerManager playerManager;
    private int boardTickCounter = 0;

    @SuppressWarnings("ConstantConditions")
    public Boards(Game game) {
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
        FastBoard board = new FastBoard(player);
        board.updateTitle(Util.getColString(plugin.getLang().scoreboard_title));

        fullUpdate(board, player);

        boards.put(player.getUniqueId(), board);
    }

    public void fullUpdate(Player player) {
        FastBoard board = boards.get(player.getUniqueId());
        if (board == null)
            return;

        fullUpdate(board, player);
    }

    public void fullUpdate(FastBoard board, Player player) {
        List<String> lines = new ArrayList<>();

        lines.add(Util.getColString(plugin.getLang().scoreboard_line_empty));

        // alive
        lines.add(Util.getColString(plugin.getLang().scoreboard_line_1 + game.getGameArenaData().aliveCount));
        // remaining time
        lines.add(Util.getColString(plugin.getLang().scoreboard_line_2 + game.getGameArenaData().timeLeft));
        lines.add(Util.getColString(plugin.getLang().scoreboard_line_empty + " "));

        if (game.getGameArenaData().getStatus() == Status.RUNNING) {
            lines.add(Util.getColString(plugin.getLang().scoreboard_line_top));

            lines.addAll(game.gamePointData.getTopPlaces(player));

            lines.add(Util.getColString(plugin.getLang().scoreboard_line_empty + "  "));
        }

        if (game.getGamePlayerData().hadPlayed(player.getUniqueId())) {
            // kills
            lines.add(Util.getColString(plugin.getLang().scoreboard_line_self_1 + game.gamePlayerData.kills.getOrDefault(player.getUniqueId(), 0)));
            // chests looted
            lines.add(Util.getColString(plugin.getLang().scoreboard_line_self_2 + game.gamePlayerData.chestsLooted.getOrDefault(player.getUniqueId(), 0)));
            lines.add(Util.getColString(plugin.getLang().scoreboard_line_empty + "     "));
        }

        board.updateLines(lines);
    }

    @Override
    public String toString() {
        return "Board{game=" + game + '}';
    }

    public void updateBoard() {
        boardTickCounter++;

        for (Map.Entry<UUID, FastBoard> entry : boards.entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player == null || !player.isOnline())
                continue;

            FastBoard board = entry.getValue();

            int i = 1;

            // alive
            board.updateLine(i++, Util.getColString(plugin.getLang().scoreboard_line_1 + game.getGameArenaData().aliveCount));
            // remaining time
            board.updateLine(i, Util.getColString(plugin.getLang().scoreboard_line_2 + game.getGameArenaData().timeLeft));

            if (game.getGameArenaData().getStatus() != Status.RUNNING)
                continue;

            if (boardTickCounter == 6) {
                i = 5;

                for (String placeEntry : game.gamePointData.getTopPlaces(player)) {
                    board.updateLine(i++, placeEntry);
                }

                if (playerManager.hasSpectatorData(player))
                    continue;

                i++;

                // kills
                board.updateLine(i++, Util.getColString(plugin.getLang().scoreboard_line_self_1 + game.gamePlayerData.kills.getOrDefault(player.getUniqueId(), 0)));
                // chests looted
                board.updateLine(i, Util.getColString(plugin.getLang().scoreboard_line_self_2 + game.gamePlayerData.chestsLooted.getOrDefault(player.getUniqueId(), 0)));
            }
        }

        if (boardTickCounter >= 6)
            boardTickCounter = 0;
    }
}
