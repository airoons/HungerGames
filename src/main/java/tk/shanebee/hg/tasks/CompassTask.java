package tk.shanebee.hg.tasks;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import tk.shanebee.hg.game.Game;
import tk.shanebee.hg.HG;
import tk.shanebee.hg.data.PlayerData;
import tk.shanebee.hg.listeners.ChestDrop;
import tk.shanebee.hg.managers.PlayerManager;

public class CompassTask implements Runnable {

	private final PlayerManager playerManager;

	public CompassTask(HG plugin) {
		this.playerManager = plugin.getPlayerManager();
		Bukkit.getScheduler().scheduleSyncRepeatingTask(HG.getPlugin(), this, 25L, 25L);
	}

	@Override
	public void run() {
		for (Player p : Bukkit.getOnlinePlayers()) {

			if (p.getInventory().contains(Material.COMPASS)) {
				PlayerData pd = playerManager.getPlayerData(p.getUniqueId());

				if (pd != null) {
					int dist = getNearestDrop(p, pd);
					String info = ChatColor.translateAlternateColorCodes('&',
							HG.getPlugin().getLang().compass_nearest_player.replace("<distance>", dist > -1 ? String.valueOf(getNearestDrop(p, pd)) : "?"));

					for (ItemStack it : p.getInventory()) {
						if (it != null && it.getType() == Material.COMPASS) {
							ItemMeta im = it.getItemMeta();
							im.setDisplayName(info);
							it.setItemMeta(im);
						}
					}
				}

			}
		}
	}

	private int cal(int i) {
		if (i < 0) {
			return -i;
		}
		return i;
	}

	private int getNearestDrop(Player p, PlayerData pd) {
		Game g = pd.getGame();

		int x = p.getLocation().getBlockX();
		int y = p.getLocation().getBlockY();
		int z = p.getLocation().getBlockZ();

		ChestDrop chestDrop =  g.getChestDrop().getLatestDrop();
		if (chestDrop != null) {
			Location l = chestDrop.getOriginal();
			p.setCompassTarget(l);

			return cal((int) (x - l.getX())) + cal((int) (y - l.getY())) + cal((int) (z - l.getZ()));
		}

		return -1;
	}
}
