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
import org.bukkit.inventory.meta.SkullMeta;
import tk.shanebee.hg.HG;
import tk.shanebee.hg.game.Game;
import tk.shanebee.hg.managers.Placeholders;
import tk.shanebee.hg.util.Util;

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
                    if (e.getCurrentItem() == null) return;
                    Player clicked = getClicked(((SkullMeta) e.getCurrentItem().getItemMeta()));
                    if (clicked == null) return;
                    e.getWhoClicked().teleport(clicked);
                }
            ));
        }
    }

    @Override
    public void update(Player player, InventoryContents contents) {}

    private ItemStack getHead(OfflinePlayer player) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = ((SkullMeta) head.getItemMeta());
        assert meta != null;
        meta.setOwningPlayer(player);
        meta.setDisplayName(Placeholders.getTeamPrefixFormatted((Player) player) + Placeholders.getTeamColor((Player) player) + player.getName());
        String[] lore = Util.getColString(HG.getPlugin().getLang().spectator_compass_head_lore).split(";");
        meta.setLore(Arrays.asList(lore));
        head.setItemMeta(meta);
        return head;
    }

    private Player getClicked(SkullMeta meta) {
        OfflinePlayer player = meta.getOwningPlayer();
        if (player == null || !player.isOnline() || !game.getGamePlayerData().getPlayers().contains(player.getUniqueId())) return null;
        return ((Player) player);
    }
}
