package com.uftype.messenger.client;

import com.uftype.messenger.auth.Authentication;
import com.uftype.messenger.common.Dispatcher;
import com.uftype.messenger.common.UserContext;
import com.uftype.messenger.gui.ClientGUI;
import com.uftype.messenger.proto.ChatMessage;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Scanner;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import static java.nio.channels.SelectionKey.OP_CONNECT;
import static java.nio.channels.SelectionKey.OP_READ;

/**
 * Represents a client dispatcher which listens to messages and sends them to the server.
 */
public class ClientDispatcher extends Dispatcher {
    private ConcurrentLinkedQueue<ChatMessage.Message> messageQueue; // Queue of incoming messages to be processed

    public ClientDispatcher(InetSocketAddress address) throws IOException {
        super(address);
        messageQueue = new ConcurrentLinkedQueue<ChatMessage.Message>();

        // Set up GUI
        ClientGUI gui = new ClientGUI(this);
        setGUI(gui);
        // Username will be set by response in GUI
    }

    /**
     * Returns a SocketChannel that the dispatcher is connected to.
     */
    @Override
    protected SelectableChannel getChannel(InetSocketAddress address) throws IOException {
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        socketChannel.connect(address);
        return socketChannel;
    }

    /**
     * Returns the demultiplexor for the client dispatcher.
     */
    @Override
    protected Selector getSelector() throws IOException {
        Selector selector = Selector.open();
        channel.register(selector, OP_CONNECT);
        return selector;
    }

    /**
     * Handle data based on type of message.
     */
    @Override
    public void handleData(ChatMessage.Message message) throws IOException {
        switch (message.getType()) {
            case LOGOUT:
                Authentication.logout(username);
                gui.addChat("Have a nice day!");
                break;
            case NEWUSER:
                if (registerUser()) {
                    // Successfully registered user
                    gui.addChat("You've successfully registered! Chat away!");
                } else {
                    gui.addChat("Uh-oh! Registration failed, please try again!");
                }
            case WHOISIN:
                String[] hosts = message.getText().trim().split("\n");
                ConcurrentHashMap<String, String> newHosts = new ConcurrentHashMap<String, String>();

                for (String host : hosts) {
                    String[] mapping = host.split("\t");
                    newHosts.put(mapping[0], mapping[1]); // Where 0 is the host, and 1 is the username
                }

                connectedHosts = newHosts;
                // Tell gui to update
                gui.updateUsers(newHosts);
                break;
            default:
                super.handleData(message);
                break;
        }
    }

    /**
     * This will be used in a command-line case.
     *
     * @return
     */
    private boolean authenticateUser() {
        Scanner in = new Scanner(System.in);
        System.out.println("Please enter your username: ");
        String username = in.nextLine();
        System.out.println("Please enter your password: ");
        String password = in.nextLine();

        UserContext user = Authentication.login(username, password);
        if (user == null) {
            return false;
        } else {
            // Handle setting up the new UserContext
            this.username = user.username;
        }

        return true;
    }

    /**
     * This will be used in a command-line case.
     *
     * @return
     */
    private boolean registerUser() {
        Scanner in = new Scanner(System.in);
        System.out.println("Please enter your first name: ");
        String firstname = in.nextLine();
        System.out.println("Please enter your last name: ");
        String lastname = in.nextLine();
        System.out.println("Please enter your username: ");
        String username = in.nextLine();
        System.out.println("Please enter your email: ");
        String email = in.nextLine();
        System.out.println("Please enter your password: ");
        String password = in.nextLine();


        if (Authentication.register(firstname, lastname, username, email, password)) {
            UserContext user = Authentication.login(username, password);
            // Handle setting up the new UserContext
            return true;
        }

        return false;
    }
}
