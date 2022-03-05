package tk.shanebee.hg.listeners;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import tk.shanebee.hg.HG;
import tk.shanebee.hg.data.Config;
import tk.shanebee.hg.game.Game;
import tk.shanebee.hg.managers.PlayerManager;
import tk.shanebee.hg.util.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;


/**
 * Manager for chest drops
 */
public class ChestDrop implements Listener {

    private Location original;
    private Chicken chicken;
    private ArrayList<Entity> passengers;
    private BlockState beforeBlock;
    private Player invopener;
    private Chunk c;
    private PlayerManager playerManager;
    private boolean chestOpened = false;

    public ChestDrop(Location original, Chicken chicken, ArrayList<Entity> passengers) {
        this.original = original;
        this.chicken = chicken;
        this.passengers = passengers;
        this.c = chicken.getLocation().getChunk();
        c.load();
        Bukkit.getPluginManager().registerEvents(this, HG.getPlugin());
        this.playerManager = HG.getPlugin().getPlayerManager();
    }

    @EventHandler
    public void onUnload(ChunkUnloadEvent event) {
        if (event.getChunk().equals(c)) {
            //event.setCancelled(true); I guess this was removed?!?
            event.getChunk().setForceLoaded(true); // Let's give this a try
        }
    }

    public void remove() {
        if (chicken != null && !chicken.isDead())
            chicken.remove();

        for (Entity passenger : passengers) {
            if (passenger == null || passenger.isDead()) continue;
            passenger.remove();
        }

        if (beforeBlock != null) {
            beforeBlock.update(true);
            Block b = beforeBlock.getBlock();
            if (b.getType() == Material.ENDER_CHEST) {
                b.setType(Material.AIR);
            }
        }


        HandlerList.unregisterAll(this);
    }

    public void placeBlock() {
        if (chicken == null) return;

        beforeBlock = chicken.getLocation().getBlock().getState();
        Location l = beforeBlock.getLocation();
        original = l;
        Util.shootFirework(new Location(l.getWorld(), l.getX() + 0.5, l.getY(), l.getZ() + 0.5));
        beforeBlock.getBlock().setType(Material.ENDER_CHEST);

        if (chicken != null && !chicken.isDead())
            chicken.remove();

        for (Entity passenger : passengers) {
            if (passenger == null || passenger.isDead()) continue;
            passenger.remove();
        }
    }

    @EventHandler
    public void onLeashSpawn(ItemSpawnEvent event) {
        if (event.getEntity().getItemStack().getType() == Material.LEAD)
            event.setCancelled(true);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        for (HumanEntity p : event.getViewers()) {
            if (p.equals(invopener)) {
                Location l = beforeBlock.getLocation();
                assert l.getWorld() != null;
                remove();
                return;
            }
        }
    }

    @EventHandler
    public void onOpenChestDrop(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && beforeBlock != null && event.getClickedBlock().getLocation().equals(beforeBlock.getLocation())) {
            Player player = event.getPlayer();
            if (!playerManager.hasPlayerData(player.getUniqueId())) return;

            if (chestOpened) {
                event.setCancelled(true);
                return;
            }
            chestOpened = true;

            Game game = playerManager.getPlayerData(player.getUniqueId()).getGame();
            Random rg = new Random();
            invopener = player;

            Inventory i = Bukkit.getServer().createInventory(player, 27);
            i.clear();
            int c = rg.nextInt(Config.randomChestMaxContent) + 1;
            c = Math.max(c, 2);

            List<Integer> slots = new ArrayList<>();
            for (int slot = 0; slot <= 26; slot++) {
                slots.add(slot);
            }
            Collections.shuffle(slots);

            while (c != 0) {
                ItemStack it = HG.getPlugin().getManager().randomItem(game,true);
                if (it != null) {
                    int slot = slots.remove(0);
                    i.setItem(slot, it);
                }
                c--;
            }

            event.setCancelled(true);
            player.playSound(player.getLocation(), Sound.BLOCK_ENDER_CHEST_OPEN, 1, 1);
            player.openInventory(i);
        }
    }

    public BlockState getBeforeBlock() {
        return beforeBlock;
    }

    public Location getOriginal() {
        return original;
    }
}
