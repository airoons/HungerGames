package tk.shanebee.hg.commands.other;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import tk.shanebee.hg.HG;
import tk.shanebee.hg.game.Game;

import java.util.ArrayList;
import java.util.List;

public class SpectatePlayerCommand implements CommandExecutor, TabCompleter {

    private final HG plugin = HG.getPlugin();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (!player.hasPermission("hg.spectate"))
                return false;

            if (args.length < 1) {
                player.sendMessage("Norādi spēlētāju!");
                return true;
            }
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null || !target.isOnline() || !target.getName().equalsIgnoreCase(args[0])) {
                player.sendMessage("Tāds spēlētājs nav online!");
                return true;
            }

            Game g = plugin.getPlayerManager().getGame(target);
            if (g == null) {
                player.sendMessage("Norādītais spēlētājs nav nevienā arēnā!");
                return true;
            }

            Game prevGame = plugin.getPlayerManager().getGame(player);
            if (prevGame != null) {
                prevGame.getGamePlayerData().leaveSpectate(player);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        g.getGamePlayerData().spectate(player, true);
                    }
                }.runTaskLater(plugin, 10L);
            } else {
                g.getGamePlayerData().spectate(player, true);
            }

            return true;
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (sender.hasPermission("hg.spectate")) {
            if (args.length == 1) {
                plugin.getGames().forEach(g -> g.getGamePlayerData().getPlayersAndSpectators().forEach(uuid -> {
                    Player pl = Bukkit.getPlayer(uuid);
                    if (pl != null && pl.isOnline())
                        completions.add(pl.getName());
                }));
            }
        }

        return StringUtil.copyPartialMatches(args[args.length - 1], completions, new ArrayList<>());
    }
}