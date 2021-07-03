package tk.shanebee.hg.gui;

import libs.fr.minuskube.inv.ClickableItem;
import libs.fr.minuskube.inv.InventoryManager;
import libs.fr.minuskube.inv.SmartInventory;
import libs.fr.minuskube.inv.content.InventoryContents;
import libs.fr.minuskube.inv.content.InventoryProvider;
import me.MrGraycat.eGlow.API.Enum.EGlowColor;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.ItemMeta;
import tk.shanebee.hg.HG;
import tk.shanebee.hg.data.Config;
import tk.shanebee.hg.game.Team;
import tk.shanebee.hg.util.Util;

import java.util.*;

public class TeamGUI implements InventoryProvider {

    private final HG plugin;
    private final InventoryManager invManager;
    private static Map<Material, EGlowColor> colorMap;
    private static Material[] bannerList;
    private Map<Integer, List<UUID>> savedPlayers;
    private SmartInventory inventory;

    public TeamGUI() {
        this.plugin = HG.getPlugin();
        this.invManager = plugin.getInventoryManager();

        if (colorMap == null) {
            colorMap = new HashMap<>();

            colorMap.put(Material.LIME_BANNER, EGlowColor.GREEN);
            colorMap.put(Material.GREEN_BANNER, EGlowColor.DARK_GREEN);
            colorMap.put(Material.YELLOW_BANNER, EGlowColor.YELLOW);
            colorMap.put(Material.ORANGE_BANNER, EGlowColor.GOLD);
            colorMap.put(Material.WHITE_BANNER, EGlowColor.WHITE);
            colorMap.put(Material.GRAY_BANNER, EGlowColor.DARK_GRAY);
            colorMap.put(Material.PINK_BANNER, EGlowColor.PINK);
            colorMap.put(Material.PURPLE_BANNER, EGlowColor.PURPLE);
            colorMap.put(Material.RED_BANNER, EGlowColor.RED);
            colorMap.put(Material.LIGHT_BLUE_BANNER, EGlowColor.AQUA);
            colorMap.put(Material.BLUE_BANNER, EGlowColor.DARK_BLUE);
            colorMap.put(Material.CYAN_BANNER, EGlowColor.DARK_AQUA);

            bannerList = new Material[12];
            bannerList[0] = Material.LIME_BANNER;
            bannerList[1] = Material.GREEN_BANNER;
            bannerList[2] = Material.YELLOW_BANNER;
            bannerList[3] = Material.ORANGE_BANNER;
            bannerList[4] = Material.WHITE_BANNER;
            bannerList[5] = Material.GRAY_BANNER;
            bannerList[6] = Material.PINK_BANNER;
            bannerList[7] = Material.PURPLE_BANNER;
            bannerList[8] = Material.RED_BANNER;
            bannerList[9] = Material.LIGHT_BLUE_BANNER;
            bannerList[10] = Material.BLUE_BANNER;
            bannerList[11] = Material.CYAN_BANNER;
        }
    }

    public void load() {
        int size = Config.total_team_count / 9 + 1;
        this.inventory = SmartInventory.builder()
                .manager(invManager)
                .provider(new TeamGUI())
                .size(Math.min(size, 6), 9)
                .title(Util.getColString(plugin.getLang().team_gui_title))
                .build();
    }

    public void open(Player player) {
        this.inventory.open(player);
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        ItemMeta itemMeta;
        savedPlayers = new HashMap<>();

        for (int i = 0; i < Config.total_team_count; i++) {
            if (i >= bannerList.length)
                break;

            ItemStack item = new ItemStack(bannerList[i]);
            itemMeta = item.getItemMeta();
            EGlowColor glowColor = colorMap.get(item.getType());
            itemMeta.setDisplayName(Util.getColString(Util.getChatColorFromGlow(glowColor) + plugin.getLang().team_colors.get(glowColor) + "&7 komanda"));


            List<String> lore = new ArrayList<>();
            List<String> list = plugin.getLang().team_select_lore;
            list.forEach(s -> {
                lore.add(Util.getColString(s));
            });

            Team team = plugin.getTeamManager().getTeam(String.valueOf(i + 1));
            if (team != null) {
                savedPlayers.put(i, team.getPlayers());
                for (UUID uuid : team.getPlayers()) {
                    OfflinePlayer p = Bukkit.getOfflinePlayer(uuid);
                    lore.add(Util.getColString(" &f" + p.getName()));
                }
            } else {
                savedPlayers.put(i, new ArrayList<>());
            }

            itemMeta.setLore(lore);
            item.setItemMeta(itemMeta);

            contents.add(ClickableItem.of(item,
                e -> attemptJoin(player, e.getSlot(), e.getCurrentItem().getType())
            ));
        }
    }

    @Override
    public void update(Player player, InventoryContents contents) {
        for (int i = 0; i < Config.total_team_count; i++) {
            Team team = plugin.getTeamManager().getTeam(String.valueOf(i + 1));
            if (team != null && !savedPlayers.get(i).equals(team.getPlayers())) {
                ItemStack item = new ItemStack(bannerList[i]);
                ItemMeta itemMeta = item.getItemMeta();
                EGlowColor glowColor = colorMap.get(item.getType());
                itemMeta.setDisplayName(Util.getColString(Util.getChatColorFromGlow(glowColor) + plugin.getLang().team_colors.get(glowColor) + "&7 komanda"));

                List<String> lore = new ArrayList<>();
                List<String> list = plugin.getLang().team_select_lore;
                list.forEach(s -> {
                    lore.add(Util.getColString(s));
                });
                for (UUID uuid : team.getPlayers()) {
                    OfflinePlayer p = Bukkit.getOfflinePlayer(uuid);
                    lore.add(Util.getColString(" &f" + p.getName()));
                }

                savedPlayers.put(i, team.getPlayers());

                itemMeta.setLore(lore);
                item.setItemMeta(itemMeta);

                contents.set(i / 9, i % 9, ClickableItem.of(item,
                    e -> attemptJoin(player, e.getSlot(), e.getCurrentItem().getType())
                ));
            }
        }
    }

    private void attemptJoin(Player player, int slot, Material material) {
        Team team = plugin.getTeamManager().getTeam(String.valueOf(slot + 1));
        Team playerTeam = plugin.getTeamManager().getTeamData(player.getUniqueId()).getTeam();

        if (playerTeam != null && playerTeam == team) {
            Util.scm(player, plugin.getLang().already_in_team);
            player.closeInventory();
            return;
        }

        if (team != null && team.getPlayers().size() >= Config.team_maxTeamSize) {
            Util.scm(player, plugin.getLang().team_max_size);
            return;
        }

        if (playerTeam != null) {
            playerTeam.leave(player, false);
        }

        if (team == null) {
            team = new Team(String.valueOf(slot + 1), colorMap.get(material));
            team.join(player);
            plugin.getTeamManager().addTeam(team);

            player.closeInventory();
            return;
        }

        team.join(player);
        player.closeInventory();
    }
}
