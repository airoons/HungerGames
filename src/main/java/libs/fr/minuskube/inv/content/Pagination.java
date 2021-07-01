package libs.fr.minuskube.inv.content;

import libs.fr.minuskube.inv.ClickableItem;

import java.util.Arrays;

public interface Pagination {

    libs.fr.minuskube.inv.ClickableItem[] getPageItems();

    int getPage();
    Pagination page(int page);

    boolean isFirst();
    boolean isLast();

    Pagination first();
    Pagination previous();
    Pagination next();
    Pagination last();

    Pagination addToIterator(SlotIterator iterator);

    Pagination setItems(libs.fr.minuskube.inv.ClickableItem... items);
    Pagination setItemsPerPage(int itemsPerPage);


    class Impl implements Pagination {

        private int currentPage;

        private libs.fr.minuskube.inv.ClickableItem[] items = new libs.fr.minuskube.inv.ClickableItem[0];
        private int itemsPerPage = 5;

        @Override
        public libs.fr.minuskube.inv.ClickableItem[] getPageItems() {
            return Arrays.copyOfRange(items,
                    currentPage * itemsPerPage,
                    (currentPage + 1) * itemsPerPage);
        }

        @Override
        public int getPage() {
            return this.currentPage;
        }

        @Override
        public Pagination page(int page) {
            this.currentPage = page;
            return this;
        }

        @Override
        public boolean isFirst() {
            return this.currentPage == 0;
        }

        @Override
        public boolean isLast() {
            int pageCount = (int) Math.ceil((double) this.items.length / this.itemsPerPage);
            return this.currentPage >= pageCount - 1;
        }

        @Override
        public Pagination first() {
            this.currentPage = 0;
            return this;
        }

        @Override
        public Pagination previous() {
            if(!isFirst())
                this.currentPage--;

            return this;
        }

        @Override
        public Pagination next() {
            if(!isLast())
                this.currentPage++;

            return this;
        }

        @Override
        public Pagination last() {
            this.currentPage = this.items.length / this.itemsPerPage;
            return this;
        }

        @Override
        public Pagination addToIterator(SlotIterator iterator) {
            for(libs.fr.minuskube.inv.ClickableItem item : getPageItems()) {
                iterator.next().set(item);

                if(iterator.ended())
                    break;
            }

            return this;
        }

        @Override
        public Pagination setItems(ClickableItem... items) {
            this.items = items;
            return this;
        }

        @Override
        public Pagination setItemsPerPage(int itemsPerPage) {
            this.itemsPerPage = itemsPerPage;
            return this;
        }

    }

}
