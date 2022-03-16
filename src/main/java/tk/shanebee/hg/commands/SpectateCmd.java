package tk.shanebee.hg.commands;

import org.bukkit.scheduler.BukkitRunnable;
import tk.shanebee.hg.game.Game;
import tk.shanebee.hg.Status;
import tk.shanebee.hg.util.Util;

public class SpectateCmd extends BaseCmd {

	public SpectateCmd() {
		forcePlayer = true;
		cmdName = "spectate";
		forceInGame = false;
		argLength = 2;
        usage = "<arena-name>";
	}

	@Override
	public boolean run() {
		if (playerManager.hasPlayerData(player)) {
			Util.scm(player, lang.cmd_join_in_game);
		} else {
			Game game = gameManager.getGame(args[1]);
			if (game != null && !game.getGamePlayerData().getPlayers().contains(player.getUniqueId())) {
				int delay = 0;
				if (playerManager.hasSpectatorData(player)) {
					delay = 10;
					playerManager.getGame(player).getGamePlayerData().leaveSpectate(player);
				}

				new BukkitRunnable() {
					@Override
					public void run() {
						Status status = game.getGameArenaData().getStatus();
						if (status != Status.BROKEN && status != Status.NOTREADY && status != Status.ROLLBACK && status != Status.STOPPED) {
							game.getGamePlayerData().spectate(player, true);
						} else {
							Util.scm(player, plugin.getLang().arena_cannot_spectate);
						}
					}
				}.runTaskLater(plugin, delay);
			} else {
				Util.scm(player, lang.cmd_delete_noexist);
			}
		}
		return true;
	}

}
