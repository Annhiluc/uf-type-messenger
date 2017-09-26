package com.uftype.messenger.common;

import com.uftype.messenger.gui.GUI;
import com.uftype.messenger.proto.ChatMessage;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.System.arraycopy;
import static java.nio.channels.SelectionKey.OP_WRITE;

/**
 * Represents the dispatcher which is employed by client and server to facilitate message sending and receiving.
 */
public abstract class Dispatcher implements Runnable {
    protected ByteBuffer buffer; // Buffer for handling messages
    protected InetSocketAddress address; // IP address
    protected volatile boolean isUp; // True if running

    public Selector selector; // Selector to demultiplex incoming channels
    public SelectableChannel channel; // Current channel
    public String username;
    public GUI gui = null;

    protected final Logger LOGGER = Logger.getLogger(Dispatcher.class.getName());

    public Dispatcher(InetSocketAddress address) throws IOException{
        this.address = address;
        this.buffer = ByteBuffer.allocate(1048567);
        this.channel = getChannel(address);
        this.selector = getSelector();
        this.isUp = true;
    }

    /**
     * Set GUI object for the dispatcher
     * @param gui
     */
    public void setGUI(GUI gui) {
        this.gui = gui;
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
     * Writes any messages connected to the handle.
     */
    public abstract void doWrite(SelectionKey handle) throws IOException;

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
            LOGGER.log(Level.WARNING, "UF TYPE server closed connection.");
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
                    } else if (handle.isWritable()) {
                        doWrite(handle);
                    }
                }
            } catch (ClosedSelectorException e) {
                gui.addChat("UF TYPE server closed connection.");
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
    protected void doAccept (SelectionKey handle) throws Exception {
        throw new Exception();
    }

    /**
     * Connect to SocketChannel.
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

    /**
     * Perform a read operation on the incoming data. Print data to the screen.
     */
    protected void doRead (SelectionKey handle) throws IOException {
            SocketChannel socketChannel = (SocketChannel) handle.channel();
            buffer.clear();

            int read;

            try {
                read = socketChannel.read(buffer);
            } catch (IOException e) {
                gui.addEvent("Another connection has disconnected.");
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

            System.out.println(data.length);

            ChatMessage.Message message = ChatMessage.Message.parseFrom(data);

            // Depending on the type of message, handle data
            handleData(message);
    }

    /**
     * Handle data based on type of message.
     */
    public void handleData (ChatMessage.Message message) throws IOException {
        switch (message.getType()) {
            case TEXT:
                String formatted = message.getUsername() + ": " + message.getText();
                gui.addChat(formatted);
                break;
            case FILE:
                try {
                    // Receive file
                    byte[] mybytearray  = message.getFile().toByteArray();
                    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(message.getText()));
                    bos.write(mybytearray, 0 , mybytearray.length);
                    gui.addEvent("File downloaded (" + mybytearray.length + " bytes read)");

                    // Close output stream
                    bos.flush();
                    //bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case CLOSE:
                gui.addChat("Another connection has disconnected.");
                break;
            default:
                break;
        }
    }
}
