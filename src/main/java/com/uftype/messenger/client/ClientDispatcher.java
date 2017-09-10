package com.uftype.messenger.client;

import com.uftype.messenger.common.Dispatcher;
import com.uftype.messenger.proto.ChatMessage;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;

import static java.nio.channels.SelectionKey.OP_CONNECT;
import static java.nio.channels.SelectionKey.OP_READ;

public class ClientDispatcher extends Dispatcher {
    private ConcurrentLinkedQueue<ChatMessage.Message> messageQueue; // Queue of incoming messages to be processed

    public ClientDispatcher(InetSocketAddress address, String username) throws IOException {
        super(address, username);
        messageQueue = new ConcurrentLinkedQueue<ChatMessage.Message>();
        LOGGER.log(Level.INFO, "Initializing UF TYPE chat client on host and port " + address.getHostName() + ":" + address.getPort());
    }

    @Override
    protected SelectableChannel getChannel(InetSocketAddress address) throws IOException {
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        socketChannel.connect(address);
        return socketChannel;
    }

    @Override
    protected Selector getSelector() throws IOException {
        Selector selector = Selector.open();
        channel.register(selector, OP_CONNECT);
        return selector;
    }

    @Override
    protected void doWrite(SelectionKey handle) throws IOException {
        SocketChannel socketChannel = (SocketChannel) handle.channel();

        while (!messageQueue.isEmpty()) {
            ChatMessage.Message toSend = messageQueue.poll();
            socketChannel.write(ByteBuffer.wrap(toSend.toByteArray()));
        }

        handle.interestOps(OP_READ);
    }

    @Override
    protected void handleData(String message) throws IOException{
        SelectionKey handle = channel.keyFor(selector);
        ChatMessage.Message chatMessage = buildMessage(message, handle.channel());
        messageQueue.add(chatMessage);
        doWrite(handle);
    }
}
