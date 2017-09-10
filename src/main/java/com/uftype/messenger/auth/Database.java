package com.uftype.messenger.auth;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Database {
    private static Connection conn;
    private final static Logger LOGGER = Logger.getLogger(Database.class.getName()); // Logger to provide information to server

    public synchronized static void connect() {
        if (conn != null) {
            // Connection is already established
            return;
        }

        // Load the Oracle JDBC driver
        try {
            DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Unable to register the JDBC driver: " + e);
        }

        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream("src/main/resources/db.properties"));
        } catch (IOException e) {
            System.out.println("Unable to read the database properties file: " + e);
        }

        // Connect to the database
        // You must put a database name after the @ sign in the connection URL.
        // You can use either the fully specified SQL*net syntax or a short cut
        // syntax as <host>:<port>:<sid>.  The example uses the short cut syntax.
        try {
            conn = DriverManager.getConnection (properties.getProperty("url"),
                    properties.getProperty("username"), properties.getProperty("password"));
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Unable to connect to the database: " + e);
        }
    }

    public synchronized static void disconnect() {
        try {
            if (conn != null) {
                conn.close(); // ** IMPORTANT : Close connections when done **
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Unable to close the database connection: " + e);
        }
    }

    public synchronized static boolean register(String firstname, String lastname, String username, String email, String securePassword, String salt) {
        try {
            // Create a Statement
            Statement stmt = conn.createStatement ();

            // Select the ENAME column from the EMP table
            ResultSet usernameCheck = stmt.executeQuery ("select * from user_account where user_name = '" + username + "'");
            ResultSet emailCheck = stmt.executeQuery ("select * from user_account where email = '" + email + "'");

            if (usernameCheck.next() || emailCheck.next()) {
                return false; // Email or username already exists
            }

            String query = "insert into user_account " +
                    "(first_name, last_name, user_name, email, password, password_salt) values ('" +
                    firstname + "', '" + lastname + "', '" + username + "', '" + email + "', '" + securePassword + "', '" + salt + "')";

            ResultSet insertUser = stmt.executeQuery(query);
            ResultSet commitUser = stmt.executeQuery("commit");
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Unable to register user: " + e);
        }

        return true;
    }

    public synchronized static boolean validate(String username, String password) {
        try {
            // Create a Statement
            Statement stmt = conn.createStatement ();

            // Select the ENAME column from the EMP table
            ResultSet rset = stmt.executeQuery ("select password, password_salt from user_account where user_name = '" + username + "'");

            if (rset.next()) {
                String salt = rset.getString("password_salt");
                String passwordToCompare = rset.getString("password");
                return Authentication.hashMD5(password, salt).equals(passwordToCompare);
            }
            else {
                return false;
            }

        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Unable to retrieve validate user information: " + e);
        }

        return false;
    }

    public static void main(String[] args) throws SQLException{
        Database.connect();

        Database.register("Test", "Name", "testuser", "test@gmail.com", Authentication.hashMD5("password", "salt"), "salt");
        System.out.println(Database.validate("testuser", "password"));

        Database.disconnect();
    }
}
