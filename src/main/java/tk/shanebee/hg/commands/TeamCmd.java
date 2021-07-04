package tk.shanebee.hg.commands;

import org.bukkit.entity.Player;
import tk.shanebee.hg.HG;
import tk.shanebee.hg.Status;
import tk.shanebee.hg.game.Game;
import tk.shanebee.hg.gui.TeamGUI;
import tk.shanebee.hg.util.Util;

public class TeamCmd extends BaseCmd {

    private static TeamGUI teamGUI;

    public TeamCmd() {
        forcePlayer = true;
        cmdName = "team";
        forceInGame = false;
        argLength = 1;

        teamGUI = new TeamGUI();
        teamGUI.load();
    }

    @Override
    public boolean run() {
        openGUI(player);
        return true;
    }

    public static void openGUI(Player player) {
        for (Game game : HG.getPlugin().getGames()) {
            if (game.getGameArenaData().getStatus() == Status.RUNNING) {
                Util.scm(player, HG.getPlugin().getLang().team_cannot_started);
                return;
            }
        }

        teamGUI.open(player);
    }
}
