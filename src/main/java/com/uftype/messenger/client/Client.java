package com.uftype.messenger.client;

import com.uftype.messenger.common.Dispatcher;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/* Represents the chat client. */
public class Client{
    private final static Logger LOGGER = Logger.getLogger(Client.class.getName()); // Logger to provide information to server

    public Client(String host, int port) throws IOException {
        LOGGER.log(Level.INFO, "The UF TYPE chat client is initialized. It is ready for chatting!");
        connect(host, port);
    }

    /* Accept communications from the port from multiple clients. Push all information onto the screen.
       Send information to clients.
     */
    private void connect(String host, int port) throws IOException {
        try {
            Dispatcher clientDispatcher = new ClientDispatcher(new InetSocketAddress(host, port));
            clientDispatcher.run();
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "UF TYPE client failure: " + e);
        }
    }

    private void disconnect() {
        // Need to disconnect from each connection in the hashmap
    }

    public static void main(String[] args) {
        try {
            Scanner in = new Scanner(System.in);

            //System.out.println("Please enter the host you want to chat with: ");
            //String host = in.nextLine();
            //System.out.println("Please enter the port you want to communicate on: ");
            //int port = in.nextInt();

            //Client client = new Client(host, port);

            Client client = new Client("127.0.0.1", 3000);
        } catch (IOException e) {
        }
    }
}



