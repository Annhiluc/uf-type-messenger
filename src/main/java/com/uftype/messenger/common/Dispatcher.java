package com.uftype.messenger.common;

import com.uftype.messenger.proto.ChatMessage;
import jdk.nashorn.internal.codegen.CompileUnit;

import java.io.IOException;
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

    protected final Logger LOGGER = Logger.getLogger(Dispatcher.class.getName());

    public Dispatcher(InetSocketAddress address, String username) throws IOException{
        this.address = address;
        this.buffer = ByteBuffer.allocate(16384);
        this.channel = getChannel(address);
        this.selector = getSelector();
        this.isUp = true;
        this.username = username;
    }

    /*
     * Returns a SelectableChannel that the dispatcher is connected to.
     */
    protected abstract SelectableChannel getChannel(InetSocketAddress address) throws IOException;

    /*
     * Returns the demultiplexor for the client dispatcher.
     */
    protected abstract Selector getSelector() throws IOException;

    /*
     * Writes any messages connected to the handle.
     */
    protected abstract void doWrite(SelectionKey handle) throws IOException;

    /*
     * Handle data based on type of message.
     */
    protected abstract void handleData (ChatMessage.Message message) throws IOException;

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
     * Accept connections; should only be implemented by ServerDispatcher.
     */
    protected void doAccept (SelectionKey handle) throws Exception {
        throw new Exception();
    }

    /*
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
                System.out.println("Another connection has disconnected.");
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

            // Depending on the type of message, handle data
            if (message.getType() == ChatMessage.Message.ChatType.TEXT) {
                String formatted = message.getUsername() + ": " + message.getText();
                System.out.println(formatted);
            }
            else {
                handleData(message);
            }
    }
}
