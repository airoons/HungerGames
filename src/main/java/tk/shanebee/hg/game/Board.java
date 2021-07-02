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
import tk.shanebee.hg.data.Config;
import tk.shanebee.hg.managers.PlayerManager;
import tk.shanebee.hg.util.Util;

import java.util.HashMap;
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

        board.set(Util.getColString(plugin.getLang().scoreboard_line_1), 10);
        board.set(Util.getColString(plugin.getLang().scoreboard_line_2), 9);
        board.set(Util.getColString(plugin.getLang().scoreboard_line_3), 8);
        // alive
        board.set(Util.getColString(plugin.getLang().scoreboard_line_4 + game.getGameArenaData().aliveCount), 7);
        // remaining time
        board.set(Util.getColString(plugin.getLang().scoreboard_line_5 + game.getGameArenaData().timeLeft), 6);
        board.set(Util.getColString(plugin.getLang().scoreboard_line_6), 5);

        if (!playerManager.hasSpectatorData(player)) {
            board.set(Util.getColString(plugin.getLang().scoreboard_line_7), 4);
            // kills
            board.set(Util.getColString(plugin.getLang().scoreboard_line_8 + game.gamePlayerData.kills.get(player.getUniqueId())), 3);
            // chests looted
            board.set(Util.getColString(plugin.getLang().scoreboard_line_9 + game.gamePlayerData.chestsLooted.get(player.getUniqueId())), 2);
            board.set(Util.getColString(plugin.getLang().scoreboard_line_10), 1);
        }

        boards.put(player.getUniqueId(), board);
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

            // alive
            entry.getValue().set(Util.getColString(plugin.getLang().scoreboard_line_4 + game.getGameArenaData().aliveCount), 7);
            // remaining time
            entry.getValue().set(Util.getColString(plugin.getLang().scoreboard_line_5 + game.getGameArenaData().timeLeft), 6);

            if (playerManager.hasSpectatorData(player))
                continue;

            // kills
            entry.getValue().set(Util.getColString(plugin.getLang().scoreboard_line_8 + game.gamePlayerData.kills.get(player.getUniqueId())), 3);
            // chests looted
            entry.getValue().set(Util.getColString(plugin.getLang().scoreboard_line_9 + game.gamePlayerData.chestsLooted.get(player.getUniqueId())), 2);
        }
    }
}
