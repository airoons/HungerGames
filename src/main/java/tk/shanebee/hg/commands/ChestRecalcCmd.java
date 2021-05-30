package tk.shanebee.hg.commands;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.data.Directional;
import tk.shanebee.hg.game.ChestData;
import tk.shanebee.hg.game.Game;
import tk.shanebee.hg.tasks.ChestReplace;
import tk.shanebee.hg.util.Util;

import java.util.Random;

public class ChestRecalcCmd extends BaseCmd {

	public ChestRecalcCmd() {
		cmdName = "chestrecalc";
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

		game.resetRandomChests();

		sender.sendMessage("Chests refilled");

		return true;
	}

}
