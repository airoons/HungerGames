package tk.shanebee.hg.listeners;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import tk.shanebee.hg.HG;
import tk.shanebee.hg.data.Config;

import java.util.ArrayList;
import java.util.List;

public class OtherListeners implements Listener {

    private final HG plugin;
    private World lobbyWorld;
    private RegionManager regManager;
    private List<Player> signVisitors = new ArrayList<>();

    public OtherListeners(HG plugin) {
        this.plugin = plugin;

        lobbyWorld = Bukkit.getWorld("world");
        if (lobbyWorld != null)
            regManager = WorldGuard.getInstance().getPlatform().getRegionContainer()
                    .get(BukkitAdapter.adapt(lobbyWorld));
    }

    @EventHandler
    public void onLobbyLeave(PlayerMoveEvent event) {
        if (event.getTo() == null || event.getTo().getWorld() == null || !event.getTo().getWorld().equals(lobbyWorld)
                || (event.getTo() != null && event.getTo().equals(event.getFrom()))
                || event.getPlayer().hasPermission("lobby.tpbypass"))
            return;

        if (regManager.getApplicableRegionsIDs(BukkitAdapter.asBlockVector(event.getFrom())).contains("lobby")
                && !regManager.getApplicableRegionsIDs(BukkitAdapter.asBlockVector(event.getTo())).contains("lobby")) {
            event.getPlayer().teleport(Config.lobbyLocation);

        } else if (!regManager.getApplicableRegionsIDs(BukkitAdapter.asBlockVector(event.getFrom())).contains("signs")
                && regManager.getApplicableRegionsIDs(BukkitAdapter.asBlockVector(event.getTo())).contains("signs")) {

            // Enters sign region

            hideSignVisitor(event.getPlayer());
        } else if (regManager.getApplicableRegionsIDs(BukkitAdapter.asBlockVector(event.getFrom())).contains("signs")
                && !regManager.getApplicableRegionsIDs(BukkitAdapter.asBlockVector(event.getTo())).contains("signs")) {

            // Exits sign region

            revealSignVisitor(event.getPlayer());
        }
    }

    public void hideSignVisitor(Player player) {
        for (Player oPlayer : signVisitors) {
            oPlayer.hidePlayer(plugin, player);
            player.hidePlayer(plugin, oPlayer);
        }
        signVisitors.add(player);
    }

    public void revealSignVisitor(Player player) {
        if (!signVisitors.remove(player))
            return;

        for (Player oPlayer : signVisitors) {
            oPlayer.showPlayer(plugin, player);
            player.showPlayer(plugin, oPlayer);
        }
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        revealSignVisitor(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        revealSignVisitor(event.getPlayer());
    }
}
