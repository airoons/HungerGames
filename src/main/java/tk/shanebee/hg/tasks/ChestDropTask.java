package tk.shanebee.hg.tasks;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import tk.shanebee.hg.HG;
import tk.shanebee.hg.Status;
import tk.shanebee.hg.data.Config;
import tk.shanebee.hg.game.Bound;
import tk.shanebee.hg.game.Game;
import tk.shanebee.hg.listeners.ChestDrop;
import tk.shanebee.hg.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChestDropTask implements Runnable {

    private final Game game;
    private final int timerID;
    private final List<ChestDrop> chests = new ArrayList<>();

    public ChestDropTask(Game game) {
        this.game = game;
        timerID = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(HG.getPlugin(), this, Config.randomChestInterval, Config.randomChestInterval);
    }

    public void run() {
        Bound bound = game.getGameArenaData().getBound();
        Integer[] i = bound.getRandomLocs();

        int x = i[0];
        int y = i[1];
        int z = i[2];
        World w = bound.getWorld();

        Block block = w.getBlockAt(x, y, z);

        while (!block.isSolid()) {
            y--;

            block = w.getBlockAt(x, y, z);
            if (y <= 0) {
                i = bound.getRandomLocs();

                x = i[0];
                y = i[1];
                z = i[2];
            }
        }

        y = y + 10;
        Location l = w.getBlockAt(x, y, z).getLocation();
        String chestLoc = HG.getPlugin().getLang().chest_drop_4
                .replace("<x>", String.valueOf(x))
                .replace("<y>", String.valueOf(y))
                .replace("<z>", String.valueOf(z));

        for (UUID u : game.getGamePlayerData().getPlayersAndSpectators()) {
            Player p = Bukkit.getPlayer(u);
            if (p != null) {
                Util.scm(p, HG.getPlugin().getLang().chest_drop_1);
                Util.scm(p, HG.getPlugin().getLang().chest_drop_3
                        .replace("<x>", String.valueOf(x))
                        .replace("<y>", String.valueOf(y))
                        .replace("<z>", String.valueOf(z)));
                Util.scm(p, chestLoc);
                Util.scm(p, HG.getPlugin().getLang().chest_drop_1);
            }
        }
        game.getGamePlayerData().soundAll(Sound.ENTITY_BAT_TAKEOFF, 1f, 1f);

        Bukkit.getScheduler().runTaskLater(game.getGameArenaData().getPlugin(), () -> {
            if (game.getGameArenaData().getStatus() != Status.RUNNING) return;

            for (UUID u : game.getGamePlayerData().getPlayersAndSpectators()) {
                Player p = Bukkit.getPlayer(u);
                if (p != null) {
                    Util.scm(p, HG.getPlugin().getLang().chest_drop_1);
                    Util.scm(p, HG.getPlugin().getLang().chest_drop_2);
                    Util.scm(p, chestLoc);
                    Util.scm(p, HG.getPlugin().getLang().chest_drop_1);
                }
            }
            FallingBlock fb = w.spawnFallingBlock(l, Bukkit.getServer().createBlockData(Material.STRIPPED_SPRUCE_WOOD));
//            Chicken chicken = (Chicken) w.spawnEntity(l, EntityType.CHICKEN);
//            chicken.addPassenger(fb);
//            chicken.setAI(false);
//            chicken.setInvisible(true);
//            chicken.setInvulnerable(true);
//            chicken.setSilent(true);
//            chicken.setPersistent(true);
//            chicken.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 99999, 1, true, false));
            fb.setDropItem(false);
            chests.add(new ChestDrop(fb));
            game.getGamePlayerData().soundAll(Sound.ENTITY_BAT_TAKEOFF, 1f, 1f);
        }, 600);
    }

    public void shutdown() {
        Bukkit.getScheduler().cancelTask(timerID);
        for (ChestDrop cd : chests) {
            if (cd != null) cd.remove();
        }
    }
}
