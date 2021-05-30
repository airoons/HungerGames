package tk.shanebee.hg.game;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;

public class ChestData {

    private Location location;
    private BlockFace blockFace;

    public ChestData(Location location, BlockFace blockFace) {
        this.location = location;
        this.blockFace = blockFace;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public BlockFace getBlockFace() {
        return blockFace;
    }

    public void setBlockFace(BlockFace blockFace) {
        this.blockFace = blockFace;
    }
}
