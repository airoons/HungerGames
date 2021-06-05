package tk.shanebee.hg.commands;

import org.bukkit.Location;
import org.bukkit.configuration.Configuration;
import tk.shanebee.hg.data.PlayerSession;
import tk.shanebee.hg.game.Bound;
import tk.shanebee.hg.game.Game;
import tk.shanebee.hg.util.Util;

import java.util.Collections;


public class SoundTestCmd extends BaseCmd {

	public SoundTestCmd() {
		forcePlayer = true;
		cmdName = "soundtest";
		argLength = 4;
		usage = "<sound> <volume> <pitch>";
	}

	@Override
	public boolean run() {
		player.playSound(player.getLocation(), args[1], Float.parseFloat(args[2]), Float.parseFloat(args[2]));

		return true;
	}
}
