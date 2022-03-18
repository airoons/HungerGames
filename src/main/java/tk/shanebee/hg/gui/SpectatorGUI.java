package tk.shanebee.hg.gui;

import libs.fr.minuskube.inv.ClickableItem;
import libs.fr.minuskube.inv.InventoryManager;
import libs.fr.minuskube.inv.SmartInventory;
import libs.fr.minuskube.inv.content.InventoryContents;
import libs.fr.minuskube.inv.content.InventoryProvider;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import tk.shanebee.hg.HG;
import tk.shanebee.hg.game.Game;
import tk.shanebee.hg.managers.Placeholders;
import tk.shanebee.hg.util.Util;

import dev.dbassett.skullcreator.SkullCreator;

import java.util.Arrays;
import java.util.UUID;

public class SpectatorGUI implements InventoryProvider {

    private final Game game;
    private final InventoryManager invManager;
    private SmartInventory inventory;

    public SpectatorGUI(Game game) {
        this.invManager = HG.getPlugin().getInventoryManager();
        this.game = game;
    }

    private void load(Game game) {
        int size = (game.getGameArenaData().getMaxPlayers() / 9) + 1;
        this.inventory = SmartInventory.builder()
                .manager(invManager)
                .provider(new SpectatorGUI(game))
                .size(Math.min(size, 6), 9)
                .title(Util.getColString(HG.getPlugin().getLang().team_gui_title))
                .build();
    }

    public void open(Player player, Game game) {
        this.load(game);
        this.inventory.open(player);
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        for (UUID uuid : game.getGamePlayerData().getPlayers()) {
            Player gPlayer = Bukkit.getPlayer(uuid);
            if (gPlayer == null) continue;
            contents.add(ClickableItem.of(getHead(gPlayer),
                e -> {
                    if (!gPlayer.isOnline() || !game.getGamePlayerData().getPlayers().contains(gPlayer.getUniqueId())) return;
                    player.teleport(gPlayer);
                }
            ));
        }
    }

    @Override
    public void update(Player player, InventoryContents contents) {}

    private ItemStack getHead(Player player) {
        ItemStack head = SkullCreator.itemFromUuid(player.getUniqueId());
        ItemMeta meta = head.getItemMeta();
        meta.setDisplayName(Util.getColString(Placeholders.getTeamPrefixFormatted(player) + Placeholders.getTeamColor(player) + player.getName()));
        String[] lore = Util.getColString(HG.getPlugin().getLang().spectator_compass_head_lore).split(";");
        meta.setLore(Arrays.asList(lore));
        head.setItemMeta(meta);
        return head;
    }
}
