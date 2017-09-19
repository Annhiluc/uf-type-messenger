package com.uftype.messenger.auth;

import com.uftype.messenger.common.UserContext;

import java.sql.*;
import java.util.logging.Logger;

/**
 * Represents a database connection for user authentication.
 */
public abstract class Database {
    protected Connection conn;
    protected final static Logger LOGGER = Logger.getLogger(Database.class.getName());

    /**
     * Returns true if can connect to the database using the username and password
     * provided in properties file, false otherwise.
     */
    public abstract boolean connect();

    /**
     * Returns true if can disconnect from the database connection, false otherwise.
     */
    public abstract boolean disconnect();

    /**
     * Returns true if database successfully registers a user, false otherwise.
     */
    public abstract boolean register(String firstname, String lastname, String username,
                                     String email, String securePassword, String salt);

    /**
     * Returns true if provided credentials are correct, false otherwise.
     */
    public abstract UserContext validate(String username, String password);
}
