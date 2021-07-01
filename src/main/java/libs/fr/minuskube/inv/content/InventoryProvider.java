package libs.fr.minuskube.inv.content;

import org.bukkit.entity.Player;

public interface InventoryProvider {

    void init(Player player, libs.fr.minuskube.inv.content.InventoryContents contents);
    default void update(Player player, InventoryContents contents) {}

}
