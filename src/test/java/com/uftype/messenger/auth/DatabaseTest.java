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
        Database.connect();
        Database.register("Test", "Name", "testuser", "test@gmail.com", Authentication.hashMD5("password", "salt"), "salt");
        Database.disconnect();
    }

    @org.junit.jupiter.api.Test
    void validate() {
        Database.connect();
        Database.validate("testuser", "password");
        Database.disconnect();
    }


}