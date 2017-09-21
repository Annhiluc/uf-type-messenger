package com.uftype.messenger.client;

import com.uftype.messenger.auth.Authentication;
import com.uftype.messenger.common.Dispatcher;
import com.uftype.messenger.common.UserContext;
import com.uftype.messenger.gui.ClientGUI;
import com.uftype.messenger.proto.ChatMessage;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Scanner;
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
     * Writes any messages to the server which are contained in the message queue.
     */
    @Override
    public void doWrite(SelectionKey handle) throws IOException {
        SocketChannel socketChannel = (SocketChannel) handle.channel();

        /*while (!messageQueue.isEmpty()) {
            ChatMessage.Message toSend = messageQueue.poll();
            socketChannel.write(ByteBuffer.wrap(toSend.toByteArray()));
        }*/

        ByteBuffer buffer = (ByteBuffer) handle.attachment();
        if (buffer != null) {
            socketChannel.write(buffer);
        }
        handle.interestOps(OP_READ);
    }

    /**
     * Handle data based on type of message.
     */
    @Override
    protected void handleData (ChatMessage.Message message) {
        switch (message.getType()) {
            case TEXT:
                String formatted = message.getUsername() + ": " + message.getText();
                gui.addChat(formatted);
                break;
            case LOGIN:
                if (authenticateUser()) {
                    // Successfully logged in
                    gui.addEvent("You've successfully logged into the messenger. Chat away!");
                }
                else {
                    gui.addEvent("You entered the wrong credentials. Please try again!");
                }
                break;
            case LOGOUT:
                Authentication.logout(username);
                gui.addChat("Have a nice day!");
                break;
            case NEWUSER:
                if (registerUser()) {
                    // Successfully registered user
                    gui.addChat("You've successfully registered! Chat away!");
                }
                else {
                    gui.addChat("Uh-oh! Registration failed, please try again!");
                }
            case CLOSE:
                gui.addChat("Another connection has disconnected.");
                break;
            default:
                break;
        }
    }

    private boolean authenticateUser() {
        Scanner in = new Scanner(System.in);
        System.out.println("Please enter your username: ");
        String username = in.nextLine();
        System.out.println("Please enter your password: ");
        String password = in.nextLine();

        UserContext user = Authentication.login(username, password);
        if (user ==  null) {
            return false;
        }
        else {
            // Handle setting up the new UserContext
            this.username = user.username;
        }

        return true;
    }

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
