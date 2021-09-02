package tk.shanebee.hg.managers;

import me.MrGraycat.eGlow.API.Enum.EGlowColor;
import tk.shanebee.hg.data.Config;
import tk.shanebee.hg.data.TeamData;
import tk.shanebee.hg.game.Team;
import tk.shanebee.hg.util.Util;

import java.util.*;

public class TeamManager {

    private final Map<UUID, TeamData> teamMap;
    private final Map<String, Team> teams;

    public TeamManager() {
        this.teamMap = new HashMap<>();
        this.teams = new HashMap<>(Config.total_team_count);

        addTeam(new Team("1", EGlowColor.GREEN));
        addTeam(new Team("2", EGlowColor.DARK_GREEN));
        addTeam(new Team("3", EGlowColor.YELLOW));
        addTeam(new Team("4", EGlowColor.GOLD));
        addTeam(new Team("5", EGlowColor.WHITE));
        addTeam(new Team("6", EGlowColor.DARK_GRAY));
        addTeam(new Team("7", EGlowColor.PINK));
        addTeam(new Team("8", EGlowColor.PURPLE));
        addTeam(new Team("9", EGlowColor.RED));
        addTeam(new Team("10", EGlowColor.AQUA));
        addTeam(new Team("11", EGlowColor.DARK_BLUE));
        addTeam(new Team("12", EGlowColor.DARK_AQUA));
    }

    public TeamData getTeamData(UUID uuid) {
        if (!teamMap.containsKey(uuid))
            teamMap.put(uuid, new TeamData(uuid));

        return teamMap.get(uuid);
    }

    public void addTeam(Team team) {
        teams.put(team.getId(), team);
    }

    public Team getTeam(String teamName) {
        return teams.get(teamName);
    }

    public void clearTeams() {
        teams.clear();
    }

    public void resetTeams() {
        for (Team team : teams.values()) {
            team.reset();
        }
    }

    public boolean hasTeam(String name) {
        return teams.containsKey(name);
    }

    public int getTeamSize() {
        return teams.size();
    }

    public Collection<Team> getTeams() {
        return teams.values();
    }
}
