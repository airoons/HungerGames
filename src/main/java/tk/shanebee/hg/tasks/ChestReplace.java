package tk.shanebee.hg.tasks;

import com.boydti.fawe.FaweAPI;
import com.boydti.fawe.bukkit.adapter.mc1_16_5.BlockMaterial_1_16_5;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.BlockVector3Imp;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.factory.CuboidRegionFactory;
import com.sk89q.worldedit.regions.factory.RegionFactory;
import com.sk89q.worldedit.regions.iterator.RegionIterator;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BlockState;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.util.BlockIterator;
import tk.shanebee.hg.HG;
import tk.shanebee.hg.game.ChestData;
import tk.shanebee.hg.game.Game;
import tk.shanebee.hg.game.GameArenaData;
import tk.shanebee.hg.util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChestReplace {

    public ChestReplace() {

    }

    public void run(Game game, CommandSender sender) {
        org.bukkit.World bukkitWorld = game.getGameArenaData().getBound().getWorld();;
        GameArenaData arenaData = game.getGameArenaData();

        BlockVector3 v1 = BukkitAdapter.asBlockVector(game.getGameArenaData().getBound().getLesserCorner());
        BlockVector3 v2 = BukkitAdapter.asBlockVector(game.getGameArenaData().getBound().getGreaterCorner());
        CuboidRegion reg = new CuboidRegion(v1, v2);

        RegionIterator iterator = new RegionIterator(reg);
        int found = 0;

        Configuration c = HG.getPlugin().getArenaConfig().getCustomConfig();
        List<String> d = new ArrayList<>();
        arenaData.chests.clear();

        sender.sendMessage("Task started...");

        while (iterator.hasNext()) {
            BlockVector3 vector = iterator.next();
            Location location = BukkitAdapter.adapt(bukkitWorld, vector);
            Block block = location.getBlock();

            if (block.getType() != Material.CHEST) {
                continue;
            }

            found++;

            arenaData.chests.add(new ChestData(block.getLocation(), ((Directional) block.getBlockData()).getFacing()));
            d.add(bukkitWorld.getName() + ":" + block.getX() + ":" + block.getY() + ":" + block.getZ() + ":" + ((Directional) block.getBlockData()).getFacing().name());
        }

        c.set("arenas." + arenaData.getName() + ".chests", d);
        HG.getPlugin().getArenaConfig().saveCustomConfig();

        sender.sendMessage("Task finished, found chests: " + found);
    }
}
