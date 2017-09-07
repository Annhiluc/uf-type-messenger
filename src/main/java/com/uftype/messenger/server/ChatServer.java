package com.uftype.messenger.server;

import com.uftype.messenger.common.ChatConnection;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChatServer {
    private ServerSocket serverSocket; // Socket for the server
    private ChatConnection connection; // Will scale to multiple connections at once

    private boolean isReceiving;

    private final static Logger LOGGER = Logger.getLogger(ChatServer.class.getName()); // Logger to provide information to server

    private HashMap<String, ChatConnection> connections;

    public ChatServer(int port) throws IOException {
        LOGGER.log(Level.INFO, "Initializing UF TYPE chat server on port " + port);
        connection = new ChatConnection();
        connections = new HashMap<String, ChatConnection>();
        LOGGER.log(Level.INFO, "The UF TYPE chat server is initialized. It is ready for chatting!");
        connect(port);
    }

    /* Accept communications from the port from multiple clients. Push all information onto the screen.
       Send information to clients.
     */
    private void connect(int port) throws IOException {
        try {
            serverSocket = new ServerSocket(port);

            while (connection.isReceiving) {
                connection.connect(serverSocket);

                connections.put(connection.socket.getInetAddress().toString(), connection);

                // People can now post in the server (chat room)
                LOGGER.log(Level.INFO, "Someone has entered the chat room: " + connection.socket.getRemoteSocketAddress());
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "UF TYPE server failure: " + e);
        }
    }

    private void disconnect() {
        connection.disconnect();
    }
}
