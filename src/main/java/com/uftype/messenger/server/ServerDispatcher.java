package com.uftype.messenger.server;


import com.uftype.messenger.common.Dispatcher;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.channels.spi.SelectorProvider;
import java.util.logging.Level;

public class ServerDispatcher extends Dispatcher {

    public ServerDispatcher(InetSocketAddress address) throws IOException {
        super(address);
        LOGGER.log(Level.INFO, "Initializing UF TYPE chat server on host and port " + address.getHostName() + ":" + address.getPort());
    }

    @Override
    protected SelectableChannel getChannel(InetSocketAddress address) throws IOException {
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        socketChannel.socket().bind(address);
        return socketChannel;
    }

    @Override
    protected Selector getSelector() throws IOException {
        Selector selector = Selector.open();
        channel.register(selector, SelectionKey.OP_ACCEPT);
        return selector;
    }

    @Override
    protected void doWrite(SelectionKey handle) throws IOException {
        SocketChannel socketChannel = (SocketChannel) handle.channel();
        ByteBuffer buffer = (ByteBuffer) handle.attachment();
        socketChannel.write(buffer);
        handle.interestOps(SelectionKey.OP_READ);
    }

    @Override
    protected void handleData(String message) throws IOException{
        for (SelectionKey key : selector.keys()) {
            if (key.channel() instanceof SocketChannel && key.equals(selector)) {
                key.attach(ByteBuffer.wrap(message.getBytes()));
                doWrite(key);
            }
        }
    }
}