package tk.shanebee.hg.commands;

import org.bukkit.entity.Player;
import tk.shanebee.hg.gui.TeamGUI;

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
        teamGUI.open(player);
    }
}
