package com.uftype.messenger.auth;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;

public class Database {
    public void connect() throws SQLException{
        // Load the Oracle JDBC driver
        DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());

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
        Connection conn = DriverManager.getConnection (properties.getProperty("url"),
                properties.getProperty("username"), properties.getProperty("password"));

        // Create a Statement
        Statement stmt = conn.createStatement ();

        // Select the ENAME column from the EMP table
        ResultSet rset = stmt.executeQuery ("select * from COURSE");

        // Iterate through the result and print the employee names
        while (rset.next())
            System.out.println (rset.getString (2));

        conn.close(); // ** IMPORTANT : Close connections when done **
    }

    public void getUser() {

    }

    public void getPassword() {

    }

    public static void main(String[] args) throws SQLException{
        Database database = new Database();
        database.connect();
    }
}
