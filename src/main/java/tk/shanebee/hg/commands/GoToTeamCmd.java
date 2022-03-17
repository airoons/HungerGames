package tk.shanebee.hg.commands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import tk.shanebee.hg.game.Game;
import tk.shanebee.hg.game.Team;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
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

        Random rand = new Random();
        int t = rand.nextInt(team.getPlayers().size());

        List<Player> players = new ArrayList<>();

        for (UUID uuid : team.getPlayers()) {
            Player target = Bukkit.getPlayer(uuid);
            if (target != null && target.isOnline() && playerManager.getGame(uuid) == g) {
                players.add(target);
            }
        }

        player.teleport(players.remove(rand.nextInt(players.size())).getLocation().add(0, 1, 0));

        return true;
    }
}
