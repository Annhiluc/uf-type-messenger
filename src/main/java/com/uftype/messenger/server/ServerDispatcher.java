package com.uftype.messenger.server;


import com.uftype.messenger.common.Dispatcher;
import com.uftype.messenger.proto.ChatMessage;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.logging.Level;

public class ServerDispatcher extends Dispatcher {

    public ServerDispatcher(InetSocketAddress address, String username) throws IOException {
        super(address, username);
        LOGGER.log(Level.INFO, "Initializing UF TYPE chat server on host and port " + address.getAddress() + ":" + address.getPort());
    }

    @Override
    protected SelectableChannel getChannel(InetSocketAddress address) throws IOException {
        ServerSocketChannel socketChannel = ServerSocketChannel.open();
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
            if (key.channel() instanceof SocketChannel && !key.equals(selector)) {
                ChatMessage.Message chatMessage = buildMessage(message, key.channel());

                // Set remote address here for message - need to change this to avoid making a builder every time
                ChatMessage.Message.Builder chatBuilder = chatMessage.toBuilder();
                chatBuilder.setRecipient(((SocketChannel) key.channel()).socket().getLocalSocketAddress().toString());
                chatMessage = chatBuilder.build();
                key.attach(ByteBuffer.wrap(chatMessage.toByteArray()));
                doWrite(key);
            }
        }
    }
}