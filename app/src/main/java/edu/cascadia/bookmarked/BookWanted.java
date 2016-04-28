package edu.cascadia.bookmarked;

import java.util.Date;

/**
 * Created by seanchung on 1/23/16.
 */
public class BookWanted {
    private String key;
    private String userId;
    private String isbn;
    private String title;
    private String author;
    private String edition;
    private String description;
    private String comment;
    private String status;
    private Date createdDate;
    private Date updatedDate;

    public BookWanted() {
    }

    public BookWanted(String userId, String isbn, String title, String author, String edition, String description, String comment ) {
        this(userId, isbn, title, author, edition, description, comment, "Active", new Date(), new Date());
    }

    public BookWanted(String userId, String isbn, String title, String author, String edition, String description, String comment, String status, Date createdDate, Date updatedDate) {
        this.userId = userId;
        this.author = author;
        this.comment = comment;
        this.description = description;
        this.edition = edition;
        this.isbn = isbn;
        this.title = title;
        this.status = status;
        this.createdDate = createdDate;
        this.updatedDate = updatedDate;
    }


    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEdition() {
        return edition;
    }

    public void setEdition(String edition) {
        this.edition = edition;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Date updatedDate) {
        this.updatedDate = updatedDate;
    }

}
