package tk.shanebee.hg.commands;

import org.bukkit.permissions.PermissionDefault;
import tk.shanebee.hg.game.Game;
import tk.shanebee.hg.util.Util;

public class TopCmd extends BaseCmd {

    public TopCmd() {
        forcePlayer = true;
        cmdName = "top";
        forceInGame = true;
        argLength = 1;
        permissionDefault = PermissionDefault.TRUE;
    }

    @Override
    public boolean run() {
        Game g = playerManager.getGame(player);

        Util.scm(player, lang.team_points_header);
        if (g == null)
            return true;

        for (String place : g.getGamePointData().getAll(player)) {
            Util.scm(player, place);
        }
        Util.scm(player, " ");

        return true;
    }

}
