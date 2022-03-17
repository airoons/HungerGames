package tk.shanebee.hg.commands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import tk.shanebee.hg.game.Game;
import tk.shanebee.hg.game.Team;
import tk.shanebee.hg.util.Util;

import java.util.UUID;

public class GoToFightCmd extends BaseCmd {

    public GoToFightCmd() {
        forcePlayer = true;
        cmdName = "gotofight";
        forceInGame = true;
    }

    @Override
    public boolean run() {
        Game g = playerManager.getGame(player);

        if (g == null || g.getGamePlayerData().lastFight == null)
            return true;

        Player target = Bukkit.getPlayer(g.getGamePlayerData().lastFight);
        if (target == null || !target.isOnline())
            return true;

        Game targetGame = playerManager.getGame(target);
        if (targetGame == null || targetGame != g || g.getGamePlayerData().getSpectators().contains(target.getUniqueId()))
            return true;

        if (target.getLocation().distanceSquared(player.getLocation()) >= 900)
            player.teleport(target.getLocation().add(0, 1, 0));

        return true;
    }

}
