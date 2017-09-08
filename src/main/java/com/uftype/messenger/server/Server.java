package com.uftype.messenger.server;

import com.uftype.messenger.common.Connection;
import com.uftype.messenger.proto.ChatMessage;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {
    private ServerSocket serverSocket; // Socket for the server
    private boolean isUp = true;
    private HashMap<String, Connection> connections = new HashMap<String, Connection>();
    private ArrayList<ChatMessage> messageQueue = new ArrayList<ChatMessage>();


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
            serverSocket = new ServerSocket(port);

            while (isUp) {
                Socket socket = serverSocket.accept();
                Connection clientConnection = new Connection(socket);
                clientConnection.start();

                connections.put(clientConnection.socket.getInetAddress().toString(), clientConnection);

                // People can now post in the server (chat room)
                LOGGER.log(Level.INFO, "Someone has entered the chat room: " + socket.getInetAddress().toString());
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "UF TYPE server failure: " + e);
        }
    }

    private void disconnect() {
        isUp = false;
        // Need to disconnect from each connection in the hashmap
    }
}
