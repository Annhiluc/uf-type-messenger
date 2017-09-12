package com.uftype.messenger.server;

import com.uftype.messenger.common.Dispatcher;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * Represents the chat server.
 */
public class Server {
    private final static Logger LOGGER = Logger.getLogger(Server.class.getName());
    private static Dispatcher serverDispatcher;

    public Server(int port) throws IOException {
        LOGGER.log(Level.INFO, "The UF TYPE chat server is initialized. It is ready for chatting!");
        connect(port);
    }

    /*
     * Accept communications from the port from multiple clients. Push all information onto the screen.
     * Broadcast information to clients.
     */
    private void connect(int port) throws IOException {
        try {
            serverDispatcher = new ServerDispatcher(new InetSocketAddress("127.0.0.1", port));
            serverDispatcher.run();
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "UF TYPE server failure: " + e);
        }
    }

    /*
     * Disconnect the server dispatcher.
     */
    private void disconnect() {
        serverDispatcher.stop();
    }

    public static void main(String[] args) {
        try {
            Server server = new Server(3000);
        } catch (IOException e) {
        }
    }
}
