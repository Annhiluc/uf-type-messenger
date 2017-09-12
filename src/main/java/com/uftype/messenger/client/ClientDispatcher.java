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

import static java.nio.channels.SelectionKey.OP_CONNECT;
import static java.nio.channels.SelectionKey.OP_READ;

/*
 * Represents a client dispatcher which listens to messages and sends them to the server.
 */
public class ClientDispatcher extends Dispatcher {
    private ConcurrentLinkedQueue<ChatMessage.Message> messageQueue; // Queue of incoming messages to be processed

    public ClientDispatcher(InetSocketAddress address, String username) throws IOException {
        super(address, username);
        messageQueue = new ConcurrentLinkedQueue<ChatMessage.Message>();
    }

    @Override
    /*
     * Returns a SocketChannel that the dispatcher is connected to.
     */
    protected SelectableChannel getChannel(InetSocketAddress address) throws IOException {
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        socketChannel.connect(address);
        return socketChannel;
    }

    @Override
    /*
     * Returns the demultiplexor for the client dispatcher.
     */
    protected Selector getSelector() throws IOException {
        Selector selector = Selector.open();
        channel.register(selector, OP_CONNECT);
        return selector;
    }

    @Override
    /*
     * Writes any messages to the server which are contained in the message queue.
     */
    protected void doWrite(SelectionKey handle) throws IOException {
        SocketChannel socketChannel = (SocketChannel) handle.channel();

        /*while (!messageQueue.isEmpty()) {
            ChatMessage.Message toSend = messageQueue.poll();
            socketChannel.write(ByteBuffer.wrap(toSend.toByteArray()));
        }*/

        ByteBuffer buffer = (ByteBuffer) handle.attachment();
        if (buffer != null) {
            socketChannel.write(buffer);
        }
        handle.interestOps(OP_READ);
    }
}
