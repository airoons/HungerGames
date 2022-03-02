package tk.shanebee.hg.tasks;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import tk.shanebee.hg.data.Config;
import tk.shanebee.hg.data.Language;
import tk.shanebee.hg.game.Game;
import tk.shanebee.hg.HG;
import tk.shanebee.hg.Status;
import tk.shanebee.hg.game.GameArenaData;

import java.util.Objects;
import java.util.UUID;

public class TimerTask implements Runnable {

	private int remainingtime;
	private final int borderCountdownStart;
	private final int borderCountdownEnd;
	private final int id;
	private final Game game;
	private final Language lang;
    private final String end_min;
    private final String end_minsec;
    private final String end_sec;
    private final int timerInterval;
	private TimerStatus status = TimerStatus.GRACE;

	public TimerTask(Game g, int time) {
		this.remainingtime = time;
		this.game = g;
		HG plugin = game.getGameArenaData().getPlugin();
		this.lang = plugin.getLang();
		this.borderCountdownStart = g.getGameBorderData().getBorderTimer().get(0);
		this.borderCountdownEnd = g.getGameBorderData().getBorderTimer().get(1);
		g.getGamePlayerData().getPlayers().forEach(uuid -> Objects.requireNonNull(Bukkit.getPlayer(uuid)).setInvulnerable(false));

		this.end_min = lang.game_ending_min;
		this.end_minsec = lang.game_ending_minsec;
		this.end_sec = lang.game_ending_sec;

		this.timerInterval = (Config.timerInterval > 0) ? Config.timerInterval : 5;

		this.id = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this, 0, timerInterval * 20L);
	}
	
	@Override
	public void run() {
		GameArenaData gameArenaData = game.getGameArenaData();
		if (game == null || gameArenaData.getStatus() != Status.RUNNING) stop();

		if (this.remainingtime <= 0) {
			if (this.status == TimerStatus.GRACE) {
				this.remainingtime = borderCountdownStart;
				this.status = TimerStatus.BORDER;
				game.getGameArenaData().setNextEvent(lang.scoreboard_stage_border);
			} else if (this.status == TimerStatus.BORDER) {
				this.remainingtime = gameArenaData.getChestRefillTime();
				this.status = TimerStatus.REFILL;
				game.getGameArenaData().setNextEvent(lang.scoreboard_stage_refill);

				int closingIn = borderCountdownEnd;
				game.getGameBorderData().setBorder(closingIn);
				game.getGamePlayerData().msgAll(lang.game_border_closing.replace("<seconds>", String.valueOf(closingIn)));
			} else if (this.status == TimerStatus.REFILL) {
				this.remainingtime = Config.gasStartTime;
				this.status = TimerStatus.GAS;
				game.getGameArenaData().setNextEvent(lang.scoreboard_stage_gas);

				game.getGameBlockData().refillChests();
				game.getGamePlayerData().msgAll(lang.game_chest_refill);
				game.getGamePlayerData().soundAll(Sound.BLOCK_FENCE_GATE_OPEN, 1f, 1f);
			} else if (this.status == TimerStatus.GAS) {
				game.getGamePlayerData().msgAll(lang.game_gas_starting);

				PotionEffect poison = new PotionEffect(PotionEffectType.POISON, 999999, 1, true, true, true);
				for (UUID u : game.getGamePlayerData().getPlayers()) {
					Player p = Bukkit.getPlayer(u);
					if (p != null) {
						p.removePotionEffect(PotionEffectType.POISON);
						p.addPotionEffect(poison);
					}
				}

				stop();
			}
		} else {
			if (this.status == TimerStatus.REFILL) {
				int refillSeconds = remainingtime - gameArenaData.getChestRefillTime();
				if (refillSeconds == 30 || refillSeconds == 10) {
					game.getGamePlayerData().msgAll(lang.game_chest_refill_in.replace("<seconds>", String.valueOf(refillSeconds)));
					game.getGamePlayerData().soundAll(Sound.BLOCK_NOTE_BLOCK_BIT, 1f, 1f);
				}
			}

//			int minutes = this.remainingtime / 60;
//			int asd = this.remainingtime % 60;
//			if (minutes != 0) {
//				if (asd == 0) {
//					if (end_min.length() < 1) return;
//					game.getGamePlayerData().msgAll(end_min.replace("<minutes>", "" + minutes));
//					game.getGamePlayerData().soundAll(Sound.BLOCK_NOTE_BLOCK_BANJO, 1f, 1f);
//				} else {
//					if (end_minsec.length() < 1) return;
//					game.getGamePlayerData().msgAll(end_minsec.replace("<minutes>", "" + minutes).replace("<seconds>", "" + asd));
//					game.getGamePlayerData().soundAll(Sound.BLOCK_NOTE_BLOCK_BANJO, 1f, 1f);
//				}
//			} else {
//				if (end_sec.length() < 1) return;
//				game.getGamePlayerData().msgAll(end_sec.replace("<seconds>", "" + this.remainingtime));
//				game.getGamePlayerData().soundAll(Sound.BLOCK_NOTE_BLOCK_BANJO, 1f, 1f);
//			}
		}
		remainingtime = remainingtime - timerInterval;

		String remainTime = "00:00";
		if (game.getTimer() != null) {
			int m = game.getTimer().getRemainingtime() / 60;
			int s = game.getTimer().getRemainingtime() % 60;
			if (s < 0)
				s = 0;

			remainTime = (m > 9 ? m : "0" + m) + ":" + (s > 9 ? s : "0" + s);
		}

		game.getGameArenaData().setTimeLeft(remainTime);
	}

	private boolean canAnnounceTime(int time) {
		switch (time) {
			case 300:
			case 180:
			case 60:
			case 30:
			case 15:
			case 5:
				return true;
			default:
				return false;
		}
	}

	public int getRemainingtime() {
		return remainingtime;
	}

	public void stop() {
		Bukkit.getScheduler().cancelTask(id);
	}

}
