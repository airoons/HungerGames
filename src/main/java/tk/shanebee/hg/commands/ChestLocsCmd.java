package tk.shanebee.hg.commands;

import tk.shanebee.hg.game.Game;
import tk.shanebee.hg.tasks.ChestReplace;
import tk.shanebee.hg.util.Util;

public class ChestLocsCmd extends BaseCmd {

	public ChestLocsCmd() {
		cmdName = "chestlocs";
		argLength = 2;
		usage = "<game>";
	}

	@Override
	public boolean run() {
		Game game = gameManager.getGame(args[1]);
		if (game == null) {
			Util.sendPrefixedMessage(sender, lang.cmd_delete_noexist);
			return true;
		}

		ChestReplace task = new ChestReplace();
		task.run(game, sender);

		return true;
	}

}
