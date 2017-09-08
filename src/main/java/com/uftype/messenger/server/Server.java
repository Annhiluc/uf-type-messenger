package com.uftype.messenger.server;

import com.uftype.messenger.common.Connection;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {
    private ServerSocket serverSocket; // Socket for the server
    private boolean isUp;

    private final static Logger LOGGER = Logger.getLogger(Server.class.getName()); // Logger to provide information to server

    private HashMap<String, Connection> connections;

    public Server(int port) throws IOException {
        LOGGER.log(Level.INFO, "Initializing UF TYPE chat server on port " + port);
        connections = new HashMap<String, Connection>();
        isUp = true;
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
                ConnectClient newConnection = new ConnectClient(socket);
                newConnection.start();
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "UF TYPE server failure: " + e);
        }
    }

    private void disconnect() {
        isUp = false;
        // Need to disconnect from each connection in the hashmap
    }

    class ConnectClient extends Thread {
        Socket socket;

        public ConnectClient(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            Connection connection = new Connection();
            connection.connect(this.socket);

            connections.put(this.socket.getInetAddress().toString(), connection);

            // People can now post in the server (chat room)
            LOGGER.log(Level.INFO, "Someone has entered the chat room: " + this.socket.getInetAddress().toString());
        }
    }
}
