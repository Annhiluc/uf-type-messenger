package com.uftype.messenger.client;

import com.uftype.messenger.common.Dispatcher;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * Represents the chat client.
 */
public class Client{
    private final static Logger LOGGER = Logger.getLogger(Client.class.getName()); // Logger to provide information to server
    private static Dispatcher clientDispatcher;

    public Client(String host, int port, String username) throws IOException {
        connect(host, port, username);
    }

    /*
     * Accept communications from the server. Pushes all information onto the screen.
     */
    private void connect(String host, int port, String username) throws IOException {
        try {
            LOGGER.log(Level.INFO, "Initializing UF TYPE chat client on host and port " + host + ":" + port);
            clientDispatcher = new ClientDispatcher(new InetSocketAddress(host, port), username);
            LOGGER.log(Level.INFO, "The UF TYPE chat client is initialized. It is ready for chatting!");
            clientDispatcher.run();
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "UF TYPE client failure: " + e);
        }
    }

    /*
     * Stops the client dispatcher from listening to requests.
     */
    private void disconnect() {
        clientDispatcher.stop();
    }

    public static void main(String[] args) {
        try {
            Scanner in = new Scanner(System.in);

            System.out.println("Please enter the username you'd like to use: ");
            String username = in.nextLine();

            // Need to do login/authentication here

            Client client = new Client("127.0.0.1", 3000, username);
        } catch (IOException e) {
        }
    }
}



