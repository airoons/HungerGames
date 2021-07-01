package tk.shanebee.hg.managers;

import tk.shanebee.hg.data.Config;
import tk.shanebee.hg.data.TeamData;
import tk.shanebee.hg.game.Team;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TeamManager {

    private final Map<UUID, TeamData> teamMap;
    private final Map<String, Team> teams;

    public TeamManager() {
        this.teamMap = new HashMap<>();
        this.teams = new HashMap<>(Config.total_team_count);

        for (int i = 0; i < Config.total_team_count; i++) {
            teams.put(String.valueOf(i + 1), null);
        }
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

    public boolean hasTeam(String name) {
        return teams.containsKey(name);
    }

    public int getTeamSize() {
        return teams.size();
    }

    public ArrayList<Team> getTeams() {
        return (ArrayList<Team>) teams.values();
    }
}
