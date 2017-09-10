package com.uftype.messenger.server;

import com.uftype.messenger.common.Dispatcher;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {
    private final static Logger LOGGER = Logger.getLogger(Server.class.getName()); // Logger to provide information to server

    public Server(int port) throws IOException {
        LOGGER.log(Level.INFO, "The UF TYPE chat server is initialized. It is ready for chatting!");
        connect(port);
    }

    /* Accept communications from the port from multiple clients. Push all information onto the screen.
       Send information to clients.
     */
    private void connect(int port) throws IOException {
        try {
            Dispatcher serverDispatcher = new ServerDispatcher(new InetSocketAddress("127.0.0.1", port), "UF TYPE Server");
            serverDispatcher.run();
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "UF TYPE server failure: " + e);
        }
    }

    private void disconnect() {
        // Need to disconnect from each connection in the hashmap
    }

    public static void main(String[] args) {
        try {
            Server server = new Server(3000);
        } catch (IOException e) {
        }
    }
}
