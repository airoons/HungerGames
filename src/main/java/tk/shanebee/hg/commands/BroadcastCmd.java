package tk.shanebee.hg.commands;

import tk.shanebee.hg.game.Game;
import tk.shanebee.hg.util.Util;

public class BroadcastCmd extends BaseCmd {

    public BroadcastCmd() {
        forcePlayer = true;
        cmdName = "broadcast";
        forceInGame = true;
        argLength = 2;
        usage = "<message>";
    }

    @Override
    public boolean run() {
        Game g = playerManager.getGame(player);
        if (g == null)
            return true;

        g.getGamePlayerData().msgAll(Util.getColString(" \n  &e" + args[1] + "\n "), false);

        return true;
    }

}
