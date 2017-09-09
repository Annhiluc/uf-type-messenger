package com.uftype.messenger.common;

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
import static java.nio.channels.SelectionKey.OP_READ;
import static java.nio.channels.SelectionKey.OP_WRITE;

public abstract class Dispatcher implements Runnable {
    protected ByteBuffer buffer; // Buffer for handling messages
    protected Selector selector; // Selector to demultiplex incoming channels
    protected SelectableChannel channel; // Current channel
    protected InetSocketAddress address; // IP address
    protected volatile boolean isUp; // True if running

    protected final Logger LOGGER = Logger.getLogger(Server.class.getName()); // Logger to provide information to server

    public Dispatcher(InetSocketAddress address) throws IOException{
        this.address = address;
        this.buffer = ByteBuffer.allocate(16384);
        this.channel = getChannel(address);
        this.selector = getSelector();
        this.isUp = true;
    }

    protected abstract SelectableChannel getChannel(InetSocketAddress address) throws IOException;
    protected abstract Selector getSelector() throws IOException;
    protected abstract void handleData(String message) throws IOException;
    protected abstract void doWrite(SelectionKey handle) throws IOException;

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
    public void run() {
        ReceiveMessages receiveThread = new ReceiveMessages();
        receiveThread.start();

        while (isUp) {
            try {
                selector.select();

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
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "UF TYPE dispatcher failure: " + e);
            }
        }

        stop();
    }

    protected void doAccept (SelectionKey handle) throws IOException {
        final ByteBuffer welcomeBuf = ByteBuffer.wrap("Welcome to UF TYPE Messenger Chat!".getBytes());

        try {
            ServerSocketChannel serverSocketChannel = (ServerSocketChannel) handle.channel();
            SocketChannel socketChannel = serverSocketChannel.accept();

            if (socketChannel != null) {
                String address = socketChannel.socket().getInetAddress() + ":" + socketChannel.socket().getPort();
                socketChannel.configureBlocking(false);
                socketChannel.register(selector, OP_READ, address);
                socketChannel.write(welcomeBuf);
                welcomeBuf.rewind();
                System.out.println("Someone entered the chat room: " + address);
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "UF TYPE accept handler failure: " + e);
        }
    }

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
        System.out.println(new String(data));
    }

    private class ReceiveMessages extends Thread {
        public void run() {
            BufferedReader in = new BufferedReader(new InputStreamReader((System.in)));
            try {
                while (isUp) {
                    String msg = in.readLine();
                    handleData(msg);
                }
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "UF TYPE receiving messages failure: " + e);
            }
        }
    }
}
