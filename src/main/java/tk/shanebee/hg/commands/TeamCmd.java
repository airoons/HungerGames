package tk.shanebee.hg.commands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import tk.shanebee.hg.data.Config;
import tk.shanebee.hg.data.PlayerData;
import tk.shanebee.hg.data.TeamData;
import tk.shanebee.hg.game.Game;
import tk.shanebee.hg.game.GamePlayerData;
import tk.shanebee.hg.game.Team;
import tk.shanebee.hg.gui.TeamGUI;
import tk.shanebee.hg.util.Util;

public class TeamCmd extends BaseCmd {

    private static TeamGUI teamGUI;

    public TeamCmd() {
        forcePlayer = true;
        cmdName = "team";
        forceInGame = false;
        argLength = 1;
//        usage = "<create/invite/accept>";

        teamGUI = new TeamGUI();
        teamGUI.load();
    }

    @Override
    public boolean run() {
        TeamData td = teamManager.getTeamData(player.getUniqueId());

        teamGUI.open(player);
//        if (args[1].equalsIgnoreCase("create")) {
//            Team team = td.getTeam();
//            if (team == null) {
//                String teamName = args.length == 3 ? args[2] : player.getName();
//                if (teamManager.hasTeam(teamName)) {
//                    String exists = lang.team_already_exists.replace("<name>", teamName);
//                    Util.scm(player, exists);
//                    return true;
//                }
//                team = new Team(player, teamName);
//                teamManager.addTeam(team);
//                String created = lang.team_created.replace("<name>", team.getName());
//                Util.scm(player, created);
//            } else {
//                String exists = lang.team_already_have.replace("<name>", team.getName());
//                Util.scm(player, exists);
//            }
//        } else if (args[1].equalsIgnoreCase("invite")) {
//            if (args.length >= 3) {
//                Team team = td.getTeam();
//                if (team == null) {
//                    Util.scm(player, lang.team_none);
//                    return true;
//                }
//                Player invitee = Bukkit.getPlayer(args[2]);
//
//                if (invitee == null) {
//                    Util.scm(player, lang.cmd_team_not_avail.replace("<player>", args[2]));
//                    return true;
//                }
//                if (invitee == player) {
//                    Util.scm(player, lang.cmd_team_self);
//                    return true;
//                }
//
//                if (!team.getLeader().equals(player.getUniqueId())) {
//                    Util.scm(player, lang.cmd_team_only_leader);
//                    return true;
//                }
//                if (team.isOnTeam(invitee.getUniqueId())) {
//                    Util.scm(player, lang.cmd_team_on_team.replace("<player>", args[2]));
//                    return true;
//                }
//                if ((team.getPlayers().size() + team.getPenders().size()) >= Config.team_maxTeamSize) {
//                    Util.scm(player, lang.cmd_team_max);
//                    return true;
//                }
//                team.invite(invitee);
//                Util.scm(player, lang.cmd_team_invited.replace("<player>", invitee.getName()));
//                return true;
//
//            } else {
//                Util.scm(player, lang.cmd_team_wrong);
//            }
//        } else if (args[1].equalsIgnoreCase("accept")) {
//            Team team = td.getPendingTeam();
//
//            if (team == null) {
//                Util.scm(player, lang.cmd_team_no_pend);
//                return true;
//            }
//            if (team.isPending(player.getUniqueId())) {
//
//                team.acceptInvite(player);
//                team.messageMembers("&6*&b&m                                                                             &6*");
//                team.messageMembers(lang.cmd_team_joined.replace("<player>", player.getName()));
//                team.messageMembers("&6*&b&m                                                                             &6*");
//                return true;
//            }
//        } else {
//            Util.scm(player, "&c" + args[1] + " is not a valid command!");
//            Util.scm(sender, sendHelpLine());
//        }
        return true;
    }

}
