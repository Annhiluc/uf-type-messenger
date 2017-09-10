package com.uftype.messenger.auth;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Authentication {
    private final static Logger LOGGER = Logger.getLogger(Authentication.class.getName()); // Logger to provide information to server
    private final static Random RANDOM = new Random();

    public static boolean login(String username, String password) {
        return authenticate(username, password);
    }

    public static boolean logout(String username) {
        return false;
    }

    public static boolean register(String firstname, String lastname, String username, String email, String password) {
        boolean isRegistered = false;
        Database.connect();

        String salt = generateSalt();
        String securePassword = hashMD5(password, salt);

        isRegistered = Database.register(firstname, lastname, username, email, securePassword, salt);

        Database.disconnect();
        return isRegistered;
    }

    public static boolean authenticate(String username, String password) {
        boolean isAuthenticated = false;
        Database.connect();

        isAuthenticated = Database.validate(username, password);

        Database.disconnect();
        return isAuthenticated;
    }

    public static String hashMD5(String password, String salt) {
        String hashed = null;

        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            String toDigest = password + salt;

            digest.update(toDigest.getBytes());
            hashed = new BigInteger(1, digest.digest()).toString(16);
        } catch (NoSuchAlgorithmException e) {
            LOGGER.log(Level.WARNING, "Unable to hash the password: " + e);
        }

        return hashed;
    }

    public static String generateSalt() {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        return salt.toString();
    }

    public void resetPassword() {

    }
}
