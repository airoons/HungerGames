package tk.shanebee.hg.tasks;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.scheduler.BukkitRunnable;
import tk.shanebee.hg.HG;
import tk.shanebee.hg.data.Language;
import tk.shanebee.hg.game.Game;
import tk.shanebee.hg.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FreeRoamTask implements Runnable {

    private final Game game;
    private final int id;
    private final int roamTime;

    public FreeRoamTask(Game game) {
        this.game = game;
        this.roamTime = game.getGameArenaData().getRoamTime();
        game.gracePeriod = true;

        Language lang = HG.getPlugin().getLang();
        String gameStarted = lang.prefix + lang.roam_game_started;
        String roamTimeString = lang.prefix + lang.roam_time.replace("<roam>", "" + roamTime);

        for (UUID u : game.getGamePlayerData().getPlayers()) {
            Player player = Bukkit.getPlayer(u);
            if (player != null) {
                Util.scm(player, gameStarted);
                if (roamTime > 0) {
                    Util.scm(player, roamTimeString);
                }
                player.setHealth(20);
                Bukkit.getPluginManager().callEvent(new EntityRegainHealthEvent(player, 0, EntityRegainHealthEvent.RegainReason.CUSTOM));
                player.setFoodLevel(20);
                game.getGamePlayerData().unFreeze(player);
                player.playSound(player.getLocation(), Sound.BLOCK_END_PORTAL_SPAWN, 1f, 1f);
            }
        }

        List<Player> gPlayers = new ArrayList<>();
        for (UUID u : game.getGamePlayerData().getPlayers()) {
            Player player = Bukkit.getPlayer(u);
            if (player != null) {
                gPlayers.add(player);
            }
        }

        List<Player> gSpectators = new ArrayList<>();
        for (UUID u : game.getGamePlayerData().getSpectators()) {
            Player player = Bukkit.getPlayer(u);
            if (player != null) {
                gSpectators.add(player);
            }
        }

        HG plugin = HG.getPlugin();

        for (Player player : gPlayers) {
            for (Player gPlayer : gPlayers) {
                player.hidePlayer(plugin, gPlayer);
                gPlayer.hidePlayer(plugin, player);
            }
            for (Player gSpectator : gSpectators) {
                gSpectator.hidePlayer(plugin, player);
            }
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : gPlayers) {
                    for (Player gPlayer : gPlayers) {
                        player.showPlayer(plugin, gPlayer);
                        gPlayer.showPlayer(plugin, player);
                    }
                    for (Player gSpectator : gSpectators) {
                        gSpectator.showPlayer(plugin, player);
                    }
                }
            }
        }.runTaskLater(plugin, 5L);
        this.id = Bukkit.getScheduler().scheduleSyncDelayedTask(HG.getPlugin(), this, roamTime * 20L);
    }

    @Override
    public void run() {
        if (roamTime > 0) {
            game.getGamePlayerData().msgAll(HG.getPlugin().getLang().roam_finished);
            game.getGamePlayerData().soundAll(Sound.BLOCK_ANVIL_LAND, 1f, 1f);
            game.gracePeriod = false;
        }
    }

    public void stop() {
        Bukkit.getScheduler().cancelTask(id);
    }

}
