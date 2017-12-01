package com.uftype.messenger.auth;

import com.uftype.messenger.common.UserContext;

import java.io.IOException;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.Level;

/**
 * Represents the database connection to CISE Oracle database.
 */
public class SqlDatabase extends Database {
    /**
     * Returns true if can connect to the database using the username and password
     * provided in properties file, false otherwise.
     */
    @Override
    public synchronized boolean connect() {
        if (conn != null) {
            // Connection is already established
            return true;
        }

        // Load the Oracle JDBC driver
        try {
            DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Unable to register the JDBC driver: " + e);
            return false;
        }

        // Try to load the properties file
        Properties properties = new Properties();
        try {
            properties.load(getClass().getClassLoader().getResourceAsStream("db.properties"));
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Unable to read the database properties file: " + e);
            return false;
        }

        // Connect to the database
        try {
            conn = DriverManager.getConnection (properties.getProperty("url"),
                    properties.getProperty("username"), properties.getProperty("password"));
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Unable to connect to the database: " + e);
            return false;
        }

        return true;
    }

    /**
     * Returns true if can disconnect from the database connection, false otherwise.
     */
    @Override
    public synchronized void disconnect() {
        try {
            if (conn != null) {
                conn.close(); // Close only if connection exists
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Unable to close the database connection: " + e);
        }
    }

    /**
     * Returns true if database successfully registers a user, false otherwise.
     */
    @Override
    public synchronized boolean register(String firstname, String lastname, String username, String email,
                                         String securePassword, String salt) {
        try {
            // Create a statement
            Statement stmt = conn.createStatement ();

            // Select users with the same username or email
            ResultSet usernameCheck = stmt.executeQuery
                    ("select id from user_account where user_name = '" + username + "'");
            ResultSet emailCheck = stmt.executeQuery
                    ("select id from user_account where email = '" + email + "'");

            if (usernameCheck.next() || emailCheck.next()) {
                return false; // Email or username already exists
            }

            // Generate query to insert user into the table
            String query = "insert into user_account " +
                    "(first_name, last_name, user_name, email, password, password_salt) " +
                    "values ('" + firstname + "', '" + lastname + "', '" + username + "', '" +
                    email + "', '" + securePassword + "', '" + salt + "')";

            stmt.executeQuery(query);
            stmt.executeQuery("commit");
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Unable to register user: " + e);
            return false;
        }

        return true;
    }

    /**
     * Returns true if provided credentials are correct, false otherwise.
     */
    @Override
    public synchronized UserContext validate(String username, String password) {
        try {
            // Create a statement
            Statement stmt = conn.createStatement ();

            // Find the user with the associated username
            ResultSet rset = stmt.executeQuery
                    ("select * from user_account where user_name = '" + username + "'");

            if (rset.next()) {
                // Compare the secure password stored with a regenerated hash with the salt
                String salt = rset.getString("password_salt");
                String passwordToCompare = rset.getString("password");

                if (Authentication.hashMD5(password, salt).equals(passwordToCompare)) {
                    return new UserContext(rset.getString("first_name"),
                            rset.getString("last_name"), rset.getString("user_name"),
                            rset.getString("email"), UserContext.Status.LOGGED_IN);
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Unable to retrieve validate user information: " + e);
        }

        return null; // Username does not exist in the database or error occurred
    }
}
