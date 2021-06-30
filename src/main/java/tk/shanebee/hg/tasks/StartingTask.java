package tk.shanebee.hg.tasks;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import tk.shanebee.hg.HG;
import tk.shanebee.hg.data.Config;
import tk.shanebee.hg.data.Language;
import tk.shanebee.hg.game.Game;
import tk.shanebee.hg.game.GameArenaData;
import tk.shanebee.hg.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class StartingTask implements Runnable {

    private int timer;
    private final int id;
    private final Game game;
    private final Language lang;

    public StartingTask(Game game) {
        HG plugin = HG.getPlugin();
        GameArenaData arenaData = game.getGameArenaData();
        this.timer = arenaData.getCountDownTime();
        this.game = game;
        this.lang = plugin.getLang();
        String name = arenaData.getName();
        String broadcast = lang.game_started
                .replace("<arena>", name)
                .replace("<seconds>", "" + timer);
        if (Config.broadcastJoinMessages) {
            Util.broadcast(broadcast);

            String joinGame = lang.game_join.replace("<arena>", name);

            List<UUID> allPlayers = new ArrayList<>();
            allPlayers.addAll(game.getGamePlayerData().getPlayers());
            allPlayers.addAll(game.getGamePlayerData().getSpectators());
            for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                if (!allPlayers.contains(player.getUniqueId()))
                    Util.sendPrefixedMessage(player, joinGame);
            }
        } else {
            this.game.getGamePlayerData().msgAll(broadcast);
        }
        game.getGamePlayerData().soundAll(Sound.BLOCK_NOTE_BLOCK_BELL, 1f, 1f);
        this.id = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this, 5 * 20L, 20L);
    }

    @Override
    public void run() {
        timer -= 1;

        if (timer <= 0) {
            stop();
            game.startFreeRoam();
            game.startGame();
        } else if (timer % 10 == 0 || timer <= 5) {
            game.getGamePlayerData().msgAll(lang.game_countdown.replace("<timer>", "" + timer));
            game.getGamePlayerData().soundAll(Sound.BLOCK_NOTE_BLOCK_BELL, 1f, 1f);
        }
    }

    public void stop() {
        Bukkit.getScheduler().cancelTask(id);
    }

}
