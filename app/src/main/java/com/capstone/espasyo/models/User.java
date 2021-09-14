package com.capstone.espasyo.models;

public class User {

    private String UID;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private int userRole;
    //TODO: Must include image url

    public User(String UID, String firstName, String lastName, String email, String password, int userRole) {
        this.UID = UID;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.userRole = userRole;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }

    public String getUID() {
        return UID;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public int getUserRole() {
        return userRole;
    }


}
