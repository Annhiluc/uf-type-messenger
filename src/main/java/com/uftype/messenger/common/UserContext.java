package com.uftype.messenger.common;

/**
 * Represents user information when connecting to a server.
 */
public class UserContext {
    private String firstname;
    private String lastname;
    private String email;
    private Status status;

    public String username;

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
