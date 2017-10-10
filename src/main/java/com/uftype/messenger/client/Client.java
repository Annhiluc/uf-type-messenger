package com.uftype.messenger.client;

import com.uftype.messenger.common.Dispatcher;
import com.uftype.messenger.common.Receiver;
import com.uftype.messenger.gui.ClientGUI;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents the chat client.
 */
public class Client{
    private final static Logger LOGGER = Logger.getLogger(Client.class.getName());
    private static Dispatcher clientDispatcher;
    private static Receiver clientReceiver;

    public Client(String host, int port) throws IOException {
        connect(host, port);
    }

    /**
     * Accept communications from the server. Pushes all information onto the screen.
     */
    private void connect(String host, int port) throws IOException {
        try {
            clientDispatcher = new ClientDispatcher(new InetSocketAddress(host, port));
            // Can set client receiver if not using GUI

            clientDispatcher.gui.addEvent("The UF TYPE chat client is initialized on " +
                    host + ":" + port + ". It is ready for chatting!");

            clientDispatcher.run();
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "UF TYPE client failure: " + e);
        }
    }

    /**
     * Stops the client dispatcher from listening to requests.
     */
    private void disconnect() {
        clientReceiver = null;
        clientDispatcher.stop();
    }

    public static void main(String[] args) {
        try {
            Client client = new Client("127.0.0.1", 3000);
        } catch (IOException e) {
        }
    }
}