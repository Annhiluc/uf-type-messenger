package com.uftype.messenger.server;


import com.uftype.messenger.common.Dispatcher;
import com.uftype.messenger.proto.ChatMessage;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.logging.Level;

import static java.lang.System.arraycopy;
import static java.nio.channels.SelectionKey.OP_CONNECT;
import static java.nio.channels.SelectionKey.OP_READ;
import static java.nio.channels.SelectionKey.OP_WRITE;

public class ServerDispatcher extends Dispatcher {
    ChatMessage.Message.Builder welcome = ChatMessage.Message.newBuilder();

    public ServerDispatcher(InetSocketAddress address) throws IOException {
        super(address, "UF TYPE Server");
        welcome.setText("Welcome to UF TYPE Messenger Chat!");
        welcome.setUsername(this.username);
        welcome.setSender(address.toString());
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
    protected void doAccept (SelectionKey handle) throws IOException {

        try {
            ServerSocketChannel serverSocketChannel = (ServerSocketChannel) handle.channel();
            SocketChannel socketChannel = serverSocketChannel.accept();

            if (socketChannel != null) {
                String address = socketChannel.socket().getInetAddress() + ":" + socketChannel.socket().getPort();
                socketChannel.configureBlocking(false);
                socketChannel.register(selector, OP_READ, address);
                welcome.setRecipient(socketChannel.socket().getLocalSocketAddress().toString()); // Set recipient of this message
                socketChannel.write(ByteBuffer.wrap(welcome.build().toByteArray()));
                System.out.println("Someone entered the chat room: " + address);
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "UF TYPE accept handler failure: " + e);
        }
    }

    @Override
    protected void doWrite(SelectionKey handle) throws IOException {
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

    protected void handleData(String message) throws IOException{
        /*for (SelectionKey key : selector.keys()) {
            if (key.channel() instanceof SocketChannel && !key.equals(selector)) {
                ChatMessage.Message chatMessage = buildMessage(message, key.channel());

                // Set remote address here for message - need to change this to avoid making a builder every time
                ChatMessage.Message.Builder chatBuilder = chatMessage.toBuilder();
                chatBuilder.setRecipient(((SocketChannel) key.channel()).socket().getLocalSocketAddress().toString());
                chatMessage = chatBuilder.build();
                key.attach(ByteBuffer.wrap(chatMessage.toByteArray()));
                //doWrite(key);
                key.interestOps(OP_WRITE);
            }
        }*/
    }
}