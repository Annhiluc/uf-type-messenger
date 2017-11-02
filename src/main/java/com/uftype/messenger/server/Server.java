package com.uftype.messenger.server;

import com.uftype.messenger.common.Dispatcher;
import com.uftype.messenger.common.Receiver;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents the chat server.
 */
public class Server extends JApplet {
    private final static Logger LOGGER = Logger.getLogger(Server.class.getName());
    private static Dispatcher serverDispatcher;
    private static Receiver serverReceiver;

    public Server() throws IOException {
    }

    /**
     * Accept communications from the port from multiple clients. Push all information onto the screen.
     * Broadcast information to clients.
     */
    private void connect(int port) throws IOException {
        try {
            serverDispatcher = new ServerDispatcher(new InetSocketAddress("127.0.0.1", port));
            // Can set server receiver if not using GUI

            serverDispatcher.gui.addEvent("The UF TYPE chat server is initialized on port " +
                    port + ". It is ready for chatting!");

            serverDispatcher.run();
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "UF TYPE server failure: " + e);
            disconnect();
        }
    }

    /**
     * Disconnect the server dispatcher.
     */
    public void disconnect() {
        serverReceiver = null;
        serverDispatcher.stop();
    }

    @Override
    public void init() {
        try {
            connect(3000);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "UF TYPE server failure: " + e);
            disconnect();
        }
    }

    @Override
    public void paint(Graphics g) {

    }

    public static void main(String[] args) {
        try {
            Server server = new Server();
            server.init();
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "UF TYPE server failure: " + e);
        }
    }
}