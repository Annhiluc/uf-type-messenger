package com.uftype.messenger.server;


import com.uftype.messenger.common.Communication;
import com.uftype.messenger.common.Dispatcher;
import com.uftype.messenger.gui.ServerGUI;
import com.uftype.messenger.proto.ChatMessage;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import static java.nio.channels.SelectionKey.OP_ACCEPT;
import static java.nio.channels.SelectionKey.OP_READ;

public class ServerDispatcher extends Dispatcher {
    ChatMessage.Message.Builder welcome = ChatMessage.Message.newBuilder();
    ConcurrentHashMap<SocketChannel, String> connections = new ConcurrentHashMap<>();

    public ServerDispatcher(InetSocketAddress address) throws IOException {
        super(address);
        username = "UF TYPE Server";
        welcome.setText("Welcome to UF TYPE Messenger Chat!");
        welcome.setUsername(this.username);
        welcome.setSender(address.toString());
        welcome.setType(ChatMessage.Message.ChatType.TEXT);

        connections = new ConcurrentHashMap<>();

        // Set up ServerGUI
        ServerGUI gui = new ServerGUI(this);
        setGUI(gui);
    }

    /**
     * Returns a ServerSocketChannel that the dispatcher is connected to.
     */
    @Override
    protected SelectableChannel getChannel(InetSocketAddress address) throws IOException {
        ServerSocketChannel socketChannel = ServerSocketChannel.open();
        socketChannel.configureBlocking(false);
        socketChannel.socket().bind(address);
        return socketChannel;
    }

    /**
     * Returns the demultiplexor for the client dispatcher.
     */
    @Override
    protected Selector getSelector() throws IOException {
        Selector selector = Selector.open();
        channel.register(selector, OP_ACCEPT);
        return selector;
    }

    /**
     * Accept connections; should only be implemented by ServerDispatcher.
     */
    @Override
    protected void doAccept (SelectionKey handle) throws IOException {

        try {
            ServerSocketChannel serverSocketChannel = (ServerSocketChannel) handle.channel();
            SocketChannel socketChannel = serverSocketChannel.accept();

            if (socketChannel != null) {
                String address = socketChannel.socket().getInetAddress() + ":" + socketChannel.socket().getPort();
                socketChannel.configureBlocking(false);
                socketChannel.register(selector, OP_READ, address);

                // Build and send welcome message
                welcome.setRecipient(socketChannel.socket().getLocalSocketAddress().toString());
                socketChannel.write(ByteBuffer.wrap(welcome.build().toByteArray()));

                // Add to connections
                connections.put(socketChannel, address);
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "UF TYPE accept handler failure: " + e);
        }
    }

    /**
     * Writes any messages by broadcasting to all handles kept by the selector.
     */
    public void doWriteAll(SelectionKey handle) throws IOException {
        ByteBuffer buffer = (ByteBuffer) handle.attachment(); // Retrieve the message

        for (SelectionKey key : selector.keys()) {
            if (key.channel() instanceof SocketChannel) {
                SocketChannel socketChannel = (SocketChannel) key.channel();

                if (buffer != null) {
                    socketChannel.write(buffer);
                    buffer.flip();
                }
                key.interestOps(OP_READ);
            }
        }
    }

    /**
     * Handle data based on type of message.
     */
    @Override
    public void handleData (ChatMessage.Message message) throws IOException {
        // Get the associated key to attach message
        SelectionKey key = channel.keyFor(selector);

        switch (message.getType()) {
            // For the special case of private messaging
            case TEXT:
                if (!message.getRecipient().equals("ALL")) {
                    for (SelectionKey sk : selector.keys()) {
                        if (sk.channel() instanceof SocketChannel &&
                                connections.get((SocketChannel)(sk.channel())).equals(message.getRecipient())) {
                            sk.attach(ByteBuffer.wrap(message.toByteArray()));
                            doWrite(sk);
                            gui.addEvent("Send private message to " + message.getRecipient());
                        }
                    }
                }
                else {
                    super.handleData(message);
                }
                break;
            case LOGIN:
                // Mirror request to client side to handle
            case NEWUSER:
                // Update username
                this.connectedHosts.put(message.getSender(), message.getUsername());
                ChatMessage.Message chatMessage = Communication.buildMessage(
                        Communication.getString(this.connectedHosts), username, "ALL",
                        key.channel(), ChatMessage.Message.ChatType.WHOISIN);
                key.attach(ByteBuffer.wrap(chatMessage.toByteArray()));
                doWriteAll(key);
                gui.addEvent("Someone entered the chat room: " + address);
                break;
            case LOGOUT:
                // Need to handle here to remove from connected hosts and update
                this.connectedHosts.remove(message.getSender());
                // Build and attach message
                chatMessage = Communication.buildMessage(Communication.getString(
                        this.connectedHosts), username, "ALL",
                        key.channel(), ChatMessage.Message.ChatType.WHOISIN);
                key.attach(ByteBuffer.wrap(chatMessage.toByteArray()));
                doWriteAll(key);

                // Remove from connections
                connections.remove(key);
            default:
                super.handleData(message);
                break;
        }
    }
}