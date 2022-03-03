package tk.shanebee.hg.commands;

import tk.shanebee.hg.game.Game;
import tk.shanebee.hg.game.GameArenaData;
import tk.shanebee.hg.util.Util;

import java.util.Arrays;

public class SetName extends BaseCmd {

    public SetName() {
        forcePlayer = true;
        cmdName = "setname";
        forceInGame = false;
        argLength = 3;
        usage = "<arena-name> <name>";
    }

    @Override
    public boolean run() {
        Game game = gameManager.getGame(args[1]);
        if (game == null) {
            Util.sendPrefixedMessage(player, lang.cmd_delete_noexist);
            return true;
        }

        GameArenaData gameArenaData = game.getGameArenaData();
        String customName = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        plugin.getArenaConfig().getConfig().set("arenas." + gameArenaData.getName() + ".custom-name", customName);
        Util.sendPrefixedMessage(player, "Custom name changed to " + customName);
        gameArenaData.setCustomName(customName);

        plugin.getArenaConfig().saveCustomConfig();

        return true;
    }

}
