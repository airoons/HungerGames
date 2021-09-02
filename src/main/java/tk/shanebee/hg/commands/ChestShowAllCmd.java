package tk.shanebee.hg.commands;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.Directional;
import tk.shanebee.hg.game.ChestData;
import tk.shanebee.hg.game.Game;
import tk.shanebee.hg.tasks.ChestReplace;
import tk.shanebee.hg.util.Util;

public class ChestShowAllCmd extends BaseCmd {

	public ChestShowAllCmd() {
		cmdName = "chestshowall";
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

		World world = game.getGameArenaData().getBound().getWorld();
		Block block = null;
		Directional dir;

		for (ChestData chestData : game.getGameArenaData().chests) {
			block = world.getBlockAt(chestData.getLocation());

			if (block.getType() != Material.CHEST) {
				block.setType(Material.CHEST);
				dir = (Directional) block.getBlockData();
				dir.setFacing(chestData.getBlockFace());
				block.setBlockData(dir);
			}
		}

		return true;
	}

}
