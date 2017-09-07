package com.uftype.messenger.client;

import com.uftype.messenger.common.ChatConnection;

import java.io.*;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/* Represents the chat client. */
public class ChatClient {
    ChatConnection connection;
    private final static Logger LOGGER = Logger.getLogger(ChatClient.class.getName()); // Logger to provide information to server

    public ChatClient(String host, int port) throws IOException {
        LOGGER.log(Level.INFO, "Initializing UF TYPE chat client on host and port " + host + " " + port);
        connection = new ChatConnection();
        connect(host, port);
    }

    /* Connect to the host with port number and start communicating to server.
       Read in text from the server as well as communicate to the server.
     */
    private void connect(String host, int port) throws IOException {
        try {
            connection.connect(host, port);

            // Can now speak with server (chat room)
            LOGGER.log(Level.INFO, "You've connected to the chat room. Chat away!");
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "UF TYPE client failure: " + e);
        }
    }

    private void disConnect() {
        connection.disconnect();
    }
}



