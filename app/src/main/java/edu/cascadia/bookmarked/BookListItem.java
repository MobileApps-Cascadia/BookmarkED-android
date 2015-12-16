package edu.cascadia.bookmarked;

/**
 * Created by seanchung on 11/6/15.
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample title for user interfaces created by
 * Android template wizards.
 * <p/>
 */
public class BookListItem {

    /**
     * An array of sample (BookItem) items.
     */
    public List<BookItem> ITEMS = new ArrayList<BookItem>();

    /**
     * A map of sample (book item) items, by ID.
     */
    public Map<String, BookItem> ITEM_MAP = new HashMap<String, BookItem>();

    public void addItem(BookItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.isbn, item);
    }

}
