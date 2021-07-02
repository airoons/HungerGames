package tk.shanebee.hg.commands;

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
        teamGUI.open(player);
        return true;
    }

}
