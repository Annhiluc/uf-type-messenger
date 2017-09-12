package com.uftype.messenger.common;

/*
 * Represents user information when connecting to a server.
 */
public class UserContext {
    public String firstname;
    public String lastname;
    public String username;
    public String email;
    public Status status;

    public enum Status {
        LOGGED_IN,
        LOGGED_OUT,
        BUSY;
    }

    public UserContext(String firstname, String lastname, String username, String email, Status status) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.username = username;
        this.email = email;
        this.status = status;
    }
}
