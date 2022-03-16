package tk.shanebee.hg.commands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import tk.shanebee.hg.game.Game;
import tk.shanebee.hg.game.Team;

import java.util.UUID;

public class GoToTeamCmd extends BaseCmd {

    public GoToTeamCmd() {
        forcePlayer = true;
        cmdName = "gototeam";
        forceInGame = true;
        argLength = 2;
    }

    @Override
    public boolean run() {
        Game g = playerManager.getGame(player);
        if (g == null)
            return true;

        Team team = g.getGameTeamData().getTeam(args[1]);
        if (team == null)
            return true;

        for (UUID uuid : team.getPlayers()) {
            Player target = Bukkit.getPlayer(uuid);
            if (target != null && target.isOnline() && playerManager.getGame(uuid) == g) {
                player.teleport(target.getLocation().add(0, 1, 0));
                return true;
            }
        }

        return true;
    }
}
