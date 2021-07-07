package tk.shanebee.hg.tasks;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
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
        if (game.getTimer().getRemainingtime() < 100)
            return;

        Bound bound = game.getGameArenaData().getBound();
        Integer[] i = bound.getRandomLocs();

        int x = i[0];
        int y = i[1];
        int z = i[2];
        World w = bound.getWorld();

        Block block = w.getBlockAt(x, y, z);

        while (!block.isSolid() && !canLandAt(block)) {
            y--;

            block = w.getBlockAt(x, y, z);
            if (y <= 0 || ((block.isSolid() || block.getType() != Material.AIR) && !canLandAt(block))) {
                i = bound.getRandomLocs();

                x = i[0];
                y = i[1];
                z = i[2];

                block = w.getBlockAt(x, y, z);
            }
        }

        String chestLoc = HG.getPlugin().getLang().chest_drop_4
                .replace("<x>", String.valueOf(x))
                .replace("<y>", String.valueOf(y))
                .replace("<z>", String.valueOf(z));

        Location l = w.getBlockAt(x, y, z).getLocation();
        l.add(0.5, 65, 0.5);

        for (UUID u : game.getGamePlayerData().getPlayersAndSpectators()) {
            Player p = Bukkit.getPlayer(u);
            if (p != null) {
                Util.scm(p, HG.getPlugin().getLang().chest_drop_1);
                Util.scm(p, HG.getPlugin().getLang().chest_drop_3);
                Util.scm(p, chestLoc);
                Util.scm(p, HG.getPlugin().getLang().chest_drop_1);
            }
        }
        game.getGamePlayerData().soundAll(Sound.ENTITY_BAT_TAKEOFF, 1f, 1f);

        w.loadChunk(l.getChunk());
        l.getChunk().setForceLoaded(true);
        l.getChunk().load(true);

        ArrayList<Entity> passengers = new ArrayList<>();
        FallingBlock fb = w.spawnFallingBlock(l, Bukkit.getServer().createBlockData(Material.OBSIDIAN));
        passengers.add(fb);

        Chicken chicken = (Chicken) w.spawnEntity(l, EntityType.CHICKEN);
        silentInvisChicken(chicken);
        chicken.addPassenger(fb);
        fb.setDropItem(false);

        Giant giant = (Giant) w.spawnEntity(l, EntityType.GIANT);
        giant.setAI(false);
        giant.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 99999, 0, true, false));
        giant.getEquipment().setHelmet(new ItemStack(Material.TURTLE_HELMET));
        fb.addPassenger(giant);
        passengers.add(giant);

        l.add(0, 0.5, 0);

        Chicken bottomLeash = (Chicken) w.spawnEntity(l, EntityType.CHICKEN);
        silentInvisChicken(bottomLeash);
        passengers.add(bottomLeash);

        Location l2 = l.clone();
        l2.add(-1, 4, 0);

        Chicken rLeash1 = (Chicken) w.spawnEntity(l2, EntityType.CHICKEN);
        silentInvisChicken(rLeash1);
        rLeash1.setLeashHolder(bottomLeash);
        passengers.add(rLeash1);

        l2.add(-1, 5, 0);

        Chicken rLeash2 = (Chicken) w.spawnEntity(l2, EntityType.CHICKEN);
        silentInvisChicken(rLeash2);
        rLeash2.setLeashHolder(rLeash1);
        passengers.add(rLeash2);

        l2 = l.clone();
        l2.add(1, 4, 0);

        Chicken lLeash1 = (Chicken) w.spawnEntity(l2, EntityType.CHICKEN);
        silentInvisChicken(lLeash1);
        lLeash1.setLeashHolder(bottomLeash);
        passengers.add(lLeash1);

        l2.add(1, 5, 0);

        Chicken lLeash2 = (Chicken) w.spawnEntity(l2, EntityType.CHICKEN);
        silentInvisChicken(lLeash2);
        lLeash2.setLeashHolder(lLeash1);
        passengers.add(lLeash2);

        ChestDrop chestDrop = new ChestDrop(chicken, passengers);
        chests.add(chestDrop);
        game.getGamePlayerData().soundAll(Sound.ENTITY_BAT_TAKEOFF, 1f, 1f);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (chicken.getLocation().getBlock().getRelative(BlockFace.DOWN).getType().isSolid()) {
                    if (game.getGameArenaData().getStatus() == Status.RUNNING) {
                        chestDrop.placeBlock();

                        for (UUID u : game.getGamePlayerData().getPlayersAndSpectators()) {
                            Player p = Bukkit.getPlayer(u);
                            if (p != null) {
                                Util.scm(p, HG.getPlugin().getLang().chest_drop_1);
                                Util.scm(p, HG.getPlugin().getLang().chest_drop_2);
                                Util.scm(p, chestLoc);
                                Util.scm(p, HG.getPlugin().getLang().chest_drop_1);
                            }
                        }
                    }

                    this.cancel();
                }
            }
        }.runTaskTimer(HG.getPlugin(), 0L, 2L);
    }

    private void silentInvisChicken(Chicken chicken) {
        chicken.setInvisible(true);
        chicken.setInvulnerable(true);
        chicken.setSilent(true);
        chicken.setPersistent(true);
        chicken.setCollidable(false);
    }

    private boolean canLandAt(Block block) {
        for (Material allowed : Config.chestDropLandBlocks) {
            if (block.getType() == allowed) {
                Block checking = block;
                for (int i = 0; i < 20; i++) {
                    if (checking.getY() < 10)
                        return false;

                    checking = block.getRelative(BlockFace.DOWN);
                    if (checking.getType() == Material.AIR)
                        return false;
                }

                return true;
            }
        }

        return false;
    }

    public void shutdown() {
        Bukkit.getScheduler().cancelTask(timerID);
        for (ChestDrop cd : chests) {
            if (cd != null) cd.remove();
        }
    }

    public List<ChestDrop> getChests() {
        return chests;
    }
}
