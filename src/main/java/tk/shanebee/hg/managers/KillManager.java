package tk.shanebee.hg.managers;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import tk.shanebee.hg.HG;
import tk.shanebee.hg.data.Language;
import tk.shanebee.hg.game.Game;
import tk.shanebee.hg.util.Util;

/**
 * Manager for deaths in game
 */
public class KillManager {
    
    private Language lang = HG.getPlugin().getLang();

    /** Get the death message when a player dies of natural causes (non-entity involved deaths)
     * @param dc Cause of the damage
     * @param name Name of the player
     * @return Message that will be sent when the player dies
     */
	public String getDeathString(DamageCause dc, String name) {
		switch (dc) {
			case ENTITY_EXPLOSION:
			case BLOCK_EXPLOSION:
				return (lang.death_explosion.replace("<player>", name));
			case CUSTOM:
				return (lang.death_custom.replace("<player>", name));
			case FALL:
				return (lang.death_fall.replace("<player>", name));
			case FALLING_BLOCK:
				return (lang.death_falling_block.replace("<player>", name));
			case FIRE:
			case FIRE_TICK:
				return (lang.death_fire.replace("<player>", name));
			case PROJECTILE:
				return (lang.death_projectile.replace("<player>", name));
			case LAVA:
				return (lang.death_lava.replace("<player>", name));
			case MAGIC:
				return (lang.death_magic.replace("<player>", name));
			case SUICIDE:
				return (lang.death_suicide.replace("<player>", name));
			default:
				return (lang.death_other_cause.replace("<player>", name).replace("<cause>", dc.toString().toLowerCase()));
		}
	}

    /** Get the death message when a player is killed by an entity
     * @param player Player who died
     * @param entity Entity that killed this player
	 * @param game Active game
     * @return Death string including the victim's name and the killer
     */
	public String getKillString(Player player, Entity entity, Game game) {
		if (entity == null)
			return (lang.death_other_entity.replace("<player>", Placeholders.getTeamColorFormatted(player) + player.getName()));

		if (entity.hasMetadata("death-message")) {
			return entity.getMetadata("death-message").get(0).asString().replace("<player>", Placeholders.getTeamColorFormatted(player) + player.getName());
		}
		switch (entity.getType()) {
            case ARROW:
                if (!isShotByPlayer(entity)) {
                    return (lang.death_skeleton.replace("<player>", Placeholders.getTeamColorFormatted(player) + player.getName()));
                } else {
                    return getPlayerKillString(player, getShooter(entity), true);
                }
			case PLAYER:
				return getPlayerKillString(player, ((Player) entity), false);
			case ZOMBIE:
				return (lang.death_zombie.replace("<player>", Placeholders.getTeamColorFormatted(player) + player.getName()));
			case SKELETON:
			case SPIDER:
				return (lang.death_spider.replace("<player>", Placeholders.getTeamColorFormatted(player) + player.getName()));
			case DROWNED:
				return (lang.death_drowned.replace("<player>", Placeholders.getTeamColorFormatted(player) + player.getName()));
			case TRIDENT:
				return (lang.death_trident.replace("<player>", Placeholders.getTeamColorFormatted(player) + player.getName()));
			case STRAY:
				return (lang.death_stray.replace("<player>", Placeholders.getTeamColorFormatted(player) + player.getName()));
			default:
				return (lang.death_other_entity.replace("<player>", Placeholders.getTeamColorFormatted(player) + player.getName()));
		}
	}

	private String getPlayerKillString(Player victim, Player killer, boolean projectile) {
        String weapon;
        if (projectile) {
            weapon = "bow and arrow";
        } else if (killer.getInventory().getItemInMainHand().getType() == Material.AIR) {
            weapon = "fist";
        } else {
            weapon = killer.getInventory().getItemInMainHand().getType().name().toLowerCase().replaceAll("_", " ");
        }
        return (lang.death_player.replace("<player>", Placeholders.getTeamColorFormatted(victim) + victim.getName())
                .replace("<killer>", Placeholders.getTeamColorFormatted(killer) + killer.getName())
                .replace("<weapon>", weapon));
    }

    /** Check if the shooter was a player
     * @param projectile The arrow which hit the player
     * @return True if the arrow was shot by a player
     */
	public boolean isShotByPlayer(Entity projectile) {
        return projectile instanceof Projectile && projectile.hasMetadata("shooter");
    }

    /** Get the shooter of this arrow
     * @param projectile The arrow in question
     * @return The player which shot the arrow
     */
    public Player getShooter(Entity projectile) {
	    return Bukkit.getPlayer(projectile.getMetadata("shooter").get(0).asString());
    }
    
}
