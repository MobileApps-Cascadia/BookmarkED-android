package edu.cascadia.bookmarked;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by seanchung on 12/4/15.
 */
public class SearchBookListItem {
    /**
     * An array of sample (BookItem) items.
     */
    public List<SearchBookItem> ITEMS = new ArrayList<SearchBookItem>();

    /**
     * A map of sample (book item) items, by ID.
     */
    public Map<String, SearchBookItem> ITEM_MAP = new HashMap<String, SearchBookItem>();

    public void addItem(SearchBookItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.isbn, item);
    }
}
