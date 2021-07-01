package tk.shanebee.hg.data;

import tk.shanebee.hg.game.Team;

import java.util.UUID;

public class TeamData {

    private final UUID uuid;

    private Team team;

    public TeamData(UUID uuid) {
        this.uuid = uuid;
    }

    /**
     * Check if a player is on a team
     *
     * @param uuid Uuid of player to check
     * @return True if player is on a team
     */
    public boolean isOnTeam(UUID uuid) {
        return (team != null && team.isOnTeam(uuid));
    }

    /**
     * Get the team of this player data
     *
     * @return The team
     */
    public Team getTeam() {
        return team;
    }

    /**
     * Set the team of this player data
     *
     * @param team The team to set
     */
    public void setTeam(Team team) {
        this.team = team;
    }
}
