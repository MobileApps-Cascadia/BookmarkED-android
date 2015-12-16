package edu.cascadia.bookmarked;

import java.util.Date;

/**
 * Created by seanchung on 11/6/15.
 */
public class User {
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private Date registeredDate;

    public User(String firstName, String lastName, String email, String phone, Date registeredDate) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.registeredDate = registeredDate;
    }

    public String getEmail() {
        return email;
    }

    public String getID() {
        return email;
    }
}
