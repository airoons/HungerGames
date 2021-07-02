package tk.shanebee.hg.listeners;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandSendEvent;
import tk.shanebee.hg.data.Config;

public class CommandSendListener implements Listener {

    private Map<String, List<String>> availableCommands = new HashMap<String, List<String>>();

    public CommandSendListener() {
        if (Config.availableCommands == null)
            return;

        for (String key : Config.availableCommands.getKeys(false)) {
            if (Config.availableCommands.isList(key)) {
                availableCommands.put(key, Config.availableCommands.getStringList(key));
            }
        }
    }

    @EventHandler
    public void onCommandSend(PlayerCommandSendEvent event) {
        if (!event.getPlayer().hasPermission("join.cmdtab.bypass")) {
            event.getCommands().clear();

            for (String key : availableCommands.keySet()) {
                if (event.getPlayer().hasPermission("join.cmdtab." + key))
                    event.getCommands().addAll(availableCommands.get(key));
            }
        }
    }
}