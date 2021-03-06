package com.uftype.messenger.auth;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseTest {
    @org.junit.jupiter.api.Test
    void connect() {
    }

    @org.junit.jupiter.api.Test
    void disconnect() {
    }

    @org.junit.jupiter.api.Test
    void register() {
        Database database = new SqlDatabase();
        database.connect();
        database.register("Test", "Name", "testuser", "test@gmail.com", Authentication.hashMD5("password", "salt"), "salt");
        database.disconnect();
    }

    @org.junit.jupiter.api.Test
    void validate() {
        Database database = new SqlDatabase();
        database.connect();
        database.validate("testuser", "password");
        database.disconnect();
    }


}