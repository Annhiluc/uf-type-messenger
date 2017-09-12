package com.uftype.messenger.common;

import com.uftype.messenger.proto.ChatMessage;
import com.uftype.messenger.server.Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.System.arraycopy;
import static java.nio.channels.SelectionKey.OP_WRITE;

/*
 * Represents the dispatcher which is employed by client and server to facilitate message sending and receiving.
 */
public abstract class Dispatcher implements Runnable {
    protected ByteBuffer buffer; // Buffer for handling messages
    protected Selector selector; // Selector to demultiplex incoming channels
    protected SelectableChannel channel; // Current channel
    protected InetSocketAddress address; // IP address
    protected volatile boolean isUp; // True if running
    protected String username;

    protected final Logger LOGGER = Logger.getLogger(Server.class.getName());

    public Dispatcher(InetSocketAddress address, String username) throws IOException{
        this.address = address;
        this.buffer = ByteBuffer.allocate(16384);
        this.channel = getChannel(address);
        this.selector = getSelector();
        this.isUp = true;
        this.username = username;
    }

    protected abstract SelectableChannel getChannel(InetSocketAddress address) throws IOException;
    protected abstract Selector getSelector() throws IOException;
    protected abstract void doWrite(SelectionKey handle) throws IOException;

    /*
     * Closes all channels which are currently held by selector.
     */
    public void stop() {
        try {
            isUp = false;
            for (SelectionKey handle : selector.keys()) {
                handle.channel().close();
            }
            selector.close();
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "UF TYPE stop failure: " + e);
        }
    }

    @Override
    /*
     * Takes the next handle to handle, and calls appropriate handling function.
     */
    public void run() {
        ReceiveMessages receiveThread = new ReceiveMessages();
        receiveThread.start();

        while (isUp) {
            try {
                selector.select(); // Selects the next set of SelectionKeys to respond to

                Iterator<SelectionKey> handleIterator = selector.selectedKeys().iterator();

                while (handleIterator.hasNext()) {
                    SelectionKey handle = handleIterator.next();
                    handleIterator.remove();

                    if (handle.isAcceptable()) {
                        doAccept(handle);
                    } else if (handle.isConnectable()) {
                        doConnect(handle);
                    } else if (handle.isReadable()) {
                        doRead(handle);
                    } else if (handle.isWritable()) {
                        doWrite(handle);
                    }
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "UF TYPE dispatcher failure: " + e);
                stop();
            }
        }

        stop(); // If errors occur, stop all channels
    }

    /*
     * Accept connections; should only be implemented by ServerDispatcher
     */
    protected void doAccept (SelectionKey handle) throws Exception {
        throw new Exception();
    }

    /*
     * Connect to SocketChannel
     */
    protected void doConnect (SelectionKey handle) throws IOException {
        try {
            SocketChannel socketChannel = (SocketChannel) handle.channel();

            if (socketChannel != null) {
                socketChannel.finishConnect();
                socketChannel.configureBlocking(false);
                socketChannel.register(selector, OP_WRITE);
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "UF TYPE connect handler failure: " + e);
            handle.channel().close();
            handle.cancel();
        }
    }

    /*
     * Perform a read operation on the incoming data. Print data to the screen.
     */
    protected void doRead (SelectionKey handle) throws IOException {
        SocketChannel socketChannel = (SocketChannel) handle.channel();
        buffer.clear();

        int read;

        try {
            read = socketChannel.read(buffer);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "UF TYPE read handler failure: " + e);
            handle.cancel();
            socketChannel.close();
            return;
        }

        if (read == -1) {
            handle.channel().close();
            handle.cancel();
            return;
        }

        byte[] data = new byte[buffer.position()];
        arraycopy(buffer.array(), 0, data, 0, buffer.position());

        ChatMessage.Message message = ChatMessage.Message.parseFrom(data);
        String formatted = message.getUsername() + ": " + message.getText();

        System.out.println(formatted);
    }

    /*
     * Receive messages written to System.in and perform write operation on them.
     */
    private class ReceiveMessages extends Thread {
        public void run() {
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            try {
                while (isUp) {
                    String message = in.readLine();
                    SelectionKey key = channel.keyFor(selector); // Get the associated key to attach message
                    ChatMessage.Message chatMessage = buildMessage(message, key.channel());
                    key.attach(ByteBuffer.wrap(chatMessage.toByteArray()));
                    doWrite(key);
                    //key.interestOps(OP_WRITE);
                }
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "UF TYPE receiving messages failure: " + e);
            }

            try {
                in.close();
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "UF TYPE failure in closing buffered reader: " + e);
            }
        }
    }

    /*
     * Build ChatMessage.Message as defined in ChatMessage.proto for communication.
     */
    protected ChatMessage.Message buildMessage(String message, SelectableChannel socketChannel) throws IOException{
        ChatMessage.Message.Builder messageBuilder = ChatMessage.Message.newBuilder();

        SocketChannel channel;
        ServerSocketChannel serverChannel;
        String localAddress = "", remoteAddress = "";

        if (socketChannel instanceof SocketChannel) {
            // Represents a client channel
            channel = (SocketChannel) socketChannel;
            localAddress = channel.socket().getLocalSocketAddress().toString();
            remoteAddress = channel.socket().getRemoteSocketAddress().toString();
        }
        else {
            // Represents the server channel
            serverChannel = (ServerSocketChannel) socketChannel;
            localAddress = serverChannel.socket().getLocalSocketAddress().toString();
            remoteAddress = "ALL";
        }

        messageBuilder.setUsername(username);
        messageBuilder.setText(message);
        messageBuilder.setSender(localAddress);
        messageBuilder.setRecipient(remoteAddress);

        return messageBuilder.build();
    }
}
