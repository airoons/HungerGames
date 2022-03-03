package tk.shanebee.hg.commands;

import tk.shanebee.hg.game.Game;
import tk.shanebee.hg.game.GameArenaData;
import tk.shanebee.hg.util.Util;

import java.util.Arrays;

public class SetAuthor extends BaseCmd {

    public SetAuthor() {
        forcePlayer = true;
        cmdName = "setauthor";
        forceInGame = false;
        argLength = 3;
        usage = "<arena-name> <author>";
    }

    @Override
    public boolean run() {
        Game game = gameManager.getGame(args[1]);
        if (game == null) {
            Util.sendPrefixedMessage(player, lang.cmd_delete_noexist);
            return true;
        }

        GameArenaData gameArenaData = game.getGameArenaData();
        String author = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        plugin.getArenaConfig().getConfig().set("arenas." + gameArenaData.getName() + ".author", author);
        Util.sendPrefixedMessage(player, "Author changed to " + author);
        gameArenaData.setAuthor(author);

        plugin.getArenaConfig().saveCustomConfig();

        return true;
    }

}
