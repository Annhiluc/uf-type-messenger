package com.uftype.messenger.server;

import com.uftype.messenger.common.Dispatcher;
import com.uftype.messenger.common.Receiver;
import com.uftype.messenger.gui.ServerGUI;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents the chat server.
 */
public class Server {
    private final static Logger LOGGER = Logger.getLogger(Server.class.getName());
    private static Dispatcher serverDispatcher;
    private static Receiver serverReceiver;

    public Server(int port) throws IOException {
        connect(port);
    }

    /**
     * Accept communications from the port from multiple clients. Push all information onto the screen.
     * Broadcast information to clients.
     */
    private void connect(int port) throws IOException {
        try {
            // Set up ServerGUI
            ServerGUI gui = new ServerGUI(this);

            serverDispatcher = new ServerDispatcher(new InetSocketAddress("127.0.0.1", port), gui);
            serverReceiver = new Receiver(serverDispatcher);
            gui.addEvent("The UF TYPE chat server is initialized on port " +
                    port + ". It is ready for chatting!");

            serverReceiver.start();
            serverDispatcher.run();
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "UF TYPE server failure: " + e);
        }
    }

    /**
     * Disconnect the server dispatcher.
     */
    public void disconnect() {
        serverReceiver = null;
        serverDispatcher.stop();
    }

    public static void main(String[] args) {
        try {
            Server server = new Server(3000);
        } catch (IOException e) {
        }
    }
}