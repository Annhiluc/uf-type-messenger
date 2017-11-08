package com.uftype.messenger.common;

import com.uftype.messenger.gui.ClientGUI;
import com.uftype.messenger.gui.CodeGUI;
import com.uftype.messenger.gui.GUI;
import com.uftype.messenger.proto.ChatMessage;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.System.arraycopy;
import static java.nio.channels.SelectionKey.OP_READ;
import static java.nio.channels.SelectionKey.OP_WRITE;

/*
Create a way to do this from commandline, in case gui is not set.
 */

/**
 * Represents the dispatcher which is employed by client and server to facilitate message sending and receiving.
 */
public abstract class Dispatcher implements Runnable {
    private ByteBuffer buffer; // Buffer for handling messages
    private volatile boolean isUp; // True if running

    protected InetSocketAddress address; // IP address

    public Selector selector; // Selector to demultiplex incoming channels
    public SelectableChannel channel; // Current channel
    public String username;
    public GUI gui = null;

    public ConcurrentHashMap<String, String> connectedHosts; // Maps host names to usernames

    protected final Logger LOGGER = Logger.getLogger(Dispatcher.class.getName());

    public Dispatcher(InetSocketAddress address) throws IOException {
        this.address = address;
        this.buffer = ByteBuffer.allocate(1048567);
        this.channel = getChannel(address);
        this.selector = getSelector();
        this.connectedHosts = new ConcurrentHashMap<>();
        this.isUp = true;
    }

    /**
     * Set GUI object for the dispatcher
     */
    protected void setGUI(GUI gui) {
        this.gui = gui;
    }

    /**
     * Returns whether the dispatcher is up or not.
     */
    boolean getIsUp() {
        return isUp;
    }

    /**
     * Returns a SelectableChannel that the dispatcher is connected to.
     */
    protected abstract SelectableChannel getChannel(InetSocketAddress address) throws IOException;

    /**
     * Returns the demultiplexor for the client dispatcher.
     */
    protected abstract Selector getSelector() throws IOException;

    /**
     * Closes all channels which are currently held by selector.
     */
    public void stop() {
        try {
            isUp = false;
            for (SelectionKey handle : selector.keys()) {
                handle.channel().close();
            }
            selector.close();
        } catch (ClosedSelectorException e) {
            LOGGER.log(Level.WARNING, "UF TYPE closed connection.");
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "UF TYPE stop failure: " + e);
        }
    }

    /**
     * Takes the next handle to handle, and calls appropriate handling function.
     */
    @Override
    public void run() {
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
                    }
                }
            } catch (ClosedSelectorException e) {
                gui.addChat("UF TYPE closed connection.");
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "UF TYPE dispatcher failure: " + e);
                stop();
            }
        }

        stop(); // If errors occur, stop all channels
    }

    /**
     * Accept connections; should only be implemented by ServerDispatcher.
     */
    protected void doAccept(SelectionKey handle) throws Exception {
        throw new Exception();
    }

    /**
     * Connect to SocketChannel.
     */
    private void doConnect(SelectionKey handle) throws IOException {
        try {
            SocketChannel socketChannel = (SocketChannel) handle.channel();

            if (socketChannel != null) {
                socketChannel.finishConnect();
                socketChannel.configureBlocking(false);
                socketChannel.register(selector, OP_WRITE);

                SelectionKey key = channel.keyFor(selector);

                try {
                    while (username == null || username.equals("")) {
                        // Waiting loop
                        Thread.sleep(1000);
                    }
                } catch (InterruptedException e) {
                    // Do nothing
                }

                // Build and attach message with username
                ChatMessage.Message chatMessage = Communication.buildMessage("", username, "ALL",
                        key.channel(), ChatMessage.Message.ChatType.NEWUSER);
                key.attach(ByteBuffer.wrap(chatMessage.toByteArray()));
                doWrite(key);
            }
        } catch (ConnectException e) {
            LOGGER.log(Level.WARNING, "UF TYPE server is not up.");
            JLabel label = new JLabel("UF TYPE server is not up. Please try again later.");
            label.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 24));
            JOptionPane.showMessageDialog(gui, label);
            handle.channel().close();
            handle.cancel();
            ((ClientGUI) gui).login.dispose();
            gui.dispose();
            System.exit(0);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "UF TYPE connect handler failure: " + e);
            handle.channel().close();
            handle.cancel();
        }
    }

    /**
     * Perform a read operation on the incoming data. Print data to the screen.
     */
    private void doRead(SelectionKey handle) throws IOException {
        SocketChannel socketChannel = (SocketChannel) handle.channel();
        buffer.clear();

        int read;

        try {
            read = socketChannel.read(buffer);
        } catch (IOException e) {
            gui.addEvent("Another connection has disconnected.");
            socketChannel.close();
            return;
        }

        if (read == -1) {
            handle.channel().close();
            return;
        }

        byte[] data = new byte[buffer.position()];
        arraycopy(buffer.array(), 0, data, 0, buffer.position());

        ChatMessage.Message message = ChatMessage.Message.parseFrom(data);

        // Depending on the type of message, handle data
        handleData(message);
    }

    /**
     * Writes any messages connected to the handle.
     */
    public void doWrite(SelectionKey handle) throws IOException {
        /*while (!messageQueue.isEmpty()) {
            ChatMessage.Message toSend = messageQueue.poll();
            socketChannel.write(ByteBuffer.wrap(toSend.toByteArray()));
        }*/

        if (handle.channel() instanceof SocketChannel) {
            SocketChannel socketChannel = (SocketChannel) handle.channel();
            ByteBuffer buffer = (ByteBuffer) handle.attachment();

            if (buffer != null) {
                socketChannel.write(buffer);
            }

            handle.interestOps(OP_READ);
        }
    }

    /**
     * Handle data based on type of message.
     */
    public void handleData(ChatMessage.Message message) throws IOException {
        switch (message.getType()) {
            case TEXT:
                String formatted = message.getUsername() + ": " + message.getText();
                gui.addChat(formatted);
                break;
            case FILE:
                try {
                    // Receive file
                    byte[] mybytearray = message.getFile().toByteArray();
                    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(message.getText()));
                    bos.write(mybytearray, 0, mybytearray.length);

                    // If image, put it in the chat message
                    if (message.getText().endsWith("jpg") || message.getText().endsWith("png")) {
                        gui.addEvent(username + " sent image: ");
                        gui.addImage(new ImageIcon(message.getText()));
                        gui.addChat("\n"); // Makes it on a new line
                    }
                    gui.addEvent("File downloaded (" + mybytearray.length + " bytes read)");

                    // Close output stream
                    bos.flush();
                    //bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case CODE:
                new CodeGUI(message.getUsername(), message.getText(), message.getLanguage());
                formatted = message.getUsername() + ": sent code\n" + message.getText();
                gui.addEvent(formatted);
                break;
            case CLOSE:
                gui.addChat("Another connection has disconnected.");

                // Actually, should send message to the connection
                break;
            default:
                break;
        }
    }
}
