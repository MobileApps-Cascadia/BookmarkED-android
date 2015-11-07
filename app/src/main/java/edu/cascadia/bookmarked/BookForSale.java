package edu.cascadia.bookmarked;

/**
 * Created by seanchung on 11/6/15.
 */
public class BookForSale extends BookItem{
    private User seller;

    public BookForSale(String id, String title, User seller) {
        super(id, title);
        this.seller = seller;
    }

    @Override
    public String toString() {
        return title;
    }
}
