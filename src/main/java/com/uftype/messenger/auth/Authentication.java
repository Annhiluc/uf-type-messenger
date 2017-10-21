package com.uftype.messenger.auth;

import com.uftype.messenger.common.UserContext;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents the authentication module to register, login, and logout users.
 */
public class Authentication {
    private static Database database = new SqlDatabase();
    private final static Logger LOGGER = Logger.getLogger(Authentication.class.getName());
    private final static Random RANDOM = new Random();


    /**
     * Returns a UserContext if login is successful using provided credentials, null otherwise.
     */
    public static UserContext login(String username, String password) {
        return authenticate(username, password);
    }

    /**
     * Returns true if logout is successful using provided credentials, false otherwise.
     */
    public static boolean logout(String username) {
        return true;
    }

    /**
     * Returns true if register is successful using provided credentials, false otherwise.
     */
    public static boolean register(String firstname, String lastname, String username, String email, String password) {
        boolean isRegistered = false;
        if (database.connect()) {
            String salt = generateSalt();
            String securePassword = hashMD5(password, salt); // This secure password wil be stored in the database

            isRegistered = database.register(firstname, lastname, username, email, securePassword, salt);
            return true;
        }

        return isRegistered;
    }

    /**
     * Returns true if authentication is successful using provided credentials, false otherwise.
     */
    public static UserContext authenticate(String username, String password) {
        UserContext isAuthenticated = null;
        if (database.connect()) {
            isAuthenticated = database.validate(username, password);
        }

        return isAuthenticated;
    }

    /**
     * Returns MD5 hash of password and salt to be stored in database
     */
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

    /**
     * Returns a random 16 byte salt String.
     */
    public static String generateSalt() {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        return salt.toString();
    }

    /**
     * Resets the password of a given user.
     */
    public void resetPassword(String username, String newPassword) {

    }

    /**
     * Sends an email to the email associated with the username to provide a reset password link
     */
    public void requestResetPassword(String username) {

    }
}
