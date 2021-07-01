package libs.fr.minuskube.inv.content;

import libs.fr.minuskube.inv.ClickableItem;
import libs.fr.minuskube.inv.SmartInventory;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface InventoryContents {

    libs.fr.minuskube.inv.SmartInventory inventory();
    libs.fr.minuskube.inv.content.Pagination pagination();

    Optional<libs.fr.minuskube.inv.content.SlotIterator> iterator(String id);

    libs.fr.minuskube.inv.content.SlotIterator newIterator(String id, libs.fr.minuskube.inv.content.SlotIterator.Type type, int startRow, int startColumn);
    libs.fr.minuskube.inv.content.SlotIterator newIterator(libs.fr.minuskube.inv.content.SlotIterator.Type type, int startRow, int startColumn);

    libs.fr.minuskube.inv.content.SlotIterator newIterator(String id, libs.fr.minuskube.inv.content.SlotIterator.Type type, libs.fr.minuskube.inv.content.SlotPos startPos);
    libs.fr.minuskube.inv.content.SlotIterator newIterator(libs.fr.minuskube.inv.content.SlotIterator.Type type, libs.fr.minuskube.inv.content.SlotPos startPos);

    libs.fr.minuskube.inv.ClickableItem[][] all();

    Optional<libs.fr.minuskube.inv.content.SlotPos> firstEmpty();

    Optional<libs.fr.minuskube.inv.ClickableItem> get(int row, int column);
    Optional<libs.fr.minuskube.inv.ClickableItem> get(libs.fr.minuskube.inv.content.SlotPos slotPos);

    InventoryContents set(int row, int column, libs.fr.minuskube.inv.ClickableItem item);
    InventoryContents set(libs.fr.minuskube.inv.content.SlotPos slotPos, libs.fr.minuskube.inv.ClickableItem item);

    InventoryContents add(libs.fr.minuskube.inv.ClickableItem item);

    InventoryContents fill(libs.fr.minuskube.inv.ClickableItem item);
    InventoryContents fillRow(int row, libs.fr.minuskube.inv.ClickableItem item);
    InventoryContents fillColumn(int column, libs.fr.minuskube.inv.ClickableItem item);
    InventoryContents fillBorders(libs.fr.minuskube.inv.ClickableItem item);

    InventoryContents fillRect(int fromRow, int fromColumn,
                               int toRow, int toColumn, libs.fr.minuskube.inv.ClickableItem item);
    InventoryContents fillRect(libs.fr.minuskube.inv.content.SlotPos fromPos, libs.fr.minuskube.inv.content.SlotPos toPos, libs.fr.minuskube.inv.ClickableItem item);

    <T> T property(String name);
    <T> T property(String name, T def);

    InventoryContents setProperty(String name, Object value);

    class Impl implements InventoryContents {

        private libs.fr.minuskube.inv.SmartInventory inv;
        private UUID player;

        private libs.fr.minuskube.inv.ClickableItem[][] contents;

        private libs.fr.minuskube.inv.content.Pagination pagination = new libs.fr.minuskube.inv.content.Pagination.Impl();
        private Map<String, libs.fr.minuskube.inv.content.SlotIterator> iterators = new HashMap<>();
        private Map<String, Object> properties = new HashMap<>();

        public Impl(libs.fr.minuskube.inv.SmartInventory inv, UUID player) {
            this.inv = inv;
            this.player = player;
            this.contents = new libs.fr.minuskube.inv.ClickableItem[inv.getRows()][inv.getColumns()];
        }

        @Override
        public SmartInventory inventory() { return inv; }

        @Override
        public Pagination pagination() { return pagination; }

        @Override
        public Optional<libs.fr.minuskube.inv.content.SlotIterator> iterator(String id) {
            return Optional.ofNullable(this.iterators.get(id));
        }

        @Override
        public libs.fr.minuskube.inv.content.SlotIterator newIterator(String id, libs.fr.minuskube.inv.content.SlotIterator.Type type, int startRow, int startColumn) {
            libs.fr.minuskube.inv.content.SlotIterator iterator = new libs.fr.minuskube.inv.content.SlotIterator.Impl(this, inv,
                    type, startRow, startColumn);

            this.iterators.put(id, iterator);
            return iterator;
        }

        @Override
        public libs.fr.minuskube.inv.content.SlotIterator newIterator(String id, libs.fr.minuskube.inv.content.SlotIterator.Type type, libs.fr.minuskube.inv.content.SlotPos startPos) {
            return newIterator(id, type, startPos.getRow(), startPos.getColumn());
        }

        @Override
        public libs.fr.minuskube.inv.content.SlotIterator newIterator(libs.fr.minuskube.inv.content.SlotIterator.Type type, int startRow, int startColumn) {
            return new libs.fr.minuskube.inv.content.SlotIterator.Impl(this, inv, type, startRow, startColumn);
        }

        @Override
        public libs.fr.minuskube.inv.content.SlotIterator newIterator(SlotIterator.Type type, libs.fr.minuskube.inv.content.SlotPos startPos) {
            return newIterator(type, startPos.getRow(), startPos.getColumn());
        }

        @Override
        public libs.fr.minuskube.inv.ClickableItem[][] all() { return contents; }

        @Override
        public Optional<libs.fr.minuskube.inv.content.SlotPos> firstEmpty() {
            for (int row = 0; row < contents.length; row++) {
                for(int column = 0; column < contents[0].length; column++) {
                    if(!this.get(row, column).isPresent())
                        return Optional.of(new libs.fr.minuskube.inv.content.SlotPos(row, column));
                }
            }

            return Optional.empty();
        }

        @Override
        public Optional<libs.fr.minuskube.inv.ClickableItem> get(int row, int column) {
            if(row >= contents.length)
                return Optional.empty();
            if(column >= contents[row].length)
                return Optional.empty();

            return Optional.ofNullable(contents[row][column]);
        }

        @Override
        public Optional<libs.fr.minuskube.inv.ClickableItem> get(libs.fr.minuskube.inv.content.SlotPos slotPos) {
            return get(slotPos.getRow(), slotPos.getColumn());
        }

        @Override
        public InventoryContents set(int row, int column, libs.fr.minuskube.inv.ClickableItem item) {
            if(row >= contents.length)
                return this;
            if(column >= contents[row].length)
                return this;

            contents[row][column] = item;
            update(row, column, item != null ? item.getItem() : null);
            return this;
        }

        @Override
        public InventoryContents set(libs.fr.minuskube.inv.content.SlotPos slotPos, libs.fr.minuskube.inv.ClickableItem item) {
            return set(slotPos.getRow(), slotPos.getColumn(), item);
        }

        @Override
        public InventoryContents add(libs.fr.minuskube.inv.ClickableItem item) {
            for(int row = 0; row < contents.length; row++) {
                for(int column = 0; column < contents[0].length; column++) {
                    if(contents[row][column] == null) {
                        set(row, column, item);
                        return this;
                    }
                }
            }

            return this;
        }

        @Override
        public InventoryContents fill(libs.fr.minuskube.inv.ClickableItem item) {
            for(int row = 0; row < contents.length; row++)
                for(int column = 0; column < contents[row].length; column++)
                    set(row, column, item);

            return this;
        }

        @Override
        public InventoryContents fillRow(int row, libs.fr.minuskube.inv.ClickableItem item) {
            if(row >= contents.length)
                return this;

            for(int column = 0; column < contents[row].length; column++)
                set(row, column, item);

            return this;
        }

        @Override
        public InventoryContents fillColumn(int column, libs.fr.minuskube.inv.ClickableItem item) {
            for(int row = 0; row < contents.length; row++)
                set(row, column, item);

            return this;
        }

        @Override
        public InventoryContents fillBorders(libs.fr.minuskube.inv.ClickableItem item) {
            fillRect(0, 0, inv.getRows() - 1, inv.getColumns() - 1, item);
            return this;
        }

        @Override
        public InventoryContents fillRect(int fromRow, int fromColumn, int toRow, int toColumn, libs.fr.minuskube.inv.ClickableItem item) {
            for(int row = fromRow; row <= toRow; row++) {
                for(int column = fromColumn; column <= toColumn; column++) {
                    if(row != fromRow && row != toRow && column != fromColumn && column != toColumn)
                        continue;

                    set(row, column, item);
                }
            }

            return this;
        }

        @Override
        public InventoryContents fillRect(libs.fr.minuskube.inv.content.SlotPos fromPos, SlotPos toPos, ClickableItem item) {
            return fillRect(fromPos.getRow(), fromPos.getColumn(), toPos.getRow(), toPos.getColumn(), item);
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T property(String name) {
            return (T) properties.get(name);
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T property(String name, T def) {
            return properties.containsKey(name) ? (T) properties.get(name) : def;
        }

        @Override
        public InventoryContents setProperty(String name, Object value) {
            properties.put(name, value);
            return this;
        }

        private void update(int row, int column, ItemStack item) {
            Player currentPlayer = Bukkit.getPlayer(player);
            if(!inv.getManager().getOpenedPlayers(inv).contains(currentPlayer))
                return;

            Inventory topInventory = currentPlayer.getOpenInventory().getTopInventory();
            topInventory.setItem(inv.getColumns() * row + column, item);
        }

    }

}