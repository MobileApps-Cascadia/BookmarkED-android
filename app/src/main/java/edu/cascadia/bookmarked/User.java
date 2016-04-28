package edu.cascadia.bookmarked;

import java.util.Date;

/**
 * Created by seanchung on 11/6/15.
 */
public class User {
    private String firstName;
    private String lastName;
    private String provider;   // password or facebook
    private String email;
    private String phone;
    private String zipcode;
    private Date lastLogin;
    private Date updatedDate;
    private Date registeredDate;

    public User() {

    }

    public User(String firstName, String lastName, String email, String phone, String zipcode, Date registeredDate) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.zipcode = zipcode;
        this.registeredDate = registeredDate;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getProvider() {
        return provider;
    }

    public String getPhone() {
        return phone;
    }

    public String getZipcode() {
        return zipcode;
    }

    public Date getLastLogin() {
        return lastLogin;
    }

    public Date getUpdatedDate() {
        return updatedDate;
    }

    public Date getRegisteredDate() {
        return registeredDate;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setRegisteredDate(Date registeredDate) {
        this.registeredDate = registeredDate;
    }

    public void setUpdatedDate(Date updatedDate) {
        this.updatedDate = updatedDate;
    }

    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }

}
