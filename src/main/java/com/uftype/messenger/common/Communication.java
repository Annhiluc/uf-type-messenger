package com.uftype.messenger.common;

import com.google.protobuf.ByteString;
import com.uftype.messenger.proto.ChatMessage;

import java.io.IOException;
import java.net.Socket;
import java.nio.channels.SelectableChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents methods to help build messages to send between clients and servers.
 */
public class Communication {
    /**
     * Build ChatMessage.Message as defined in ChatMessage.proto for communication.
     */
    public static ChatMessage.Message buildMessage(String message, String username, String recipient, SelectableChannel socketChannel,
                                                   ChatMessage.Message.ChatType chatType) throws IOException {
        return buildMessage(message, null, username, recipient, socketChannel, chatType);
    }

    /**
     * Build ChatMessage.Message as defined in ChatMessage.proto for communication.
     */
    public static ChatMessage.Message buildMessage(String message, byte[] file, String username, String recipient, SelectableChannel socketChannel,
                                                   ChatMessage.Message.ChatType chatType) throws IOException {
        ChatMessage.Message.Builder messageBuilder = initializeMessage(socketChannel);
        messageBuilder.setUsername(username);
        if (message != null) {
            messageBuilder.setText(message);
        }
        messageBuilder.setRecipient(recipient); // Should be a host address or "ALL"
        if (file != null) {
            messageBuilder.setFile(ByteString.copyFrom(file));
        }
        messageBuilder.setType(chatType);

        return messageBuilder.build();
    }

    /**
     * Sets up a message builder with the username, recipient, and sender of the message.
     * The text, type, and username of the message still need to be set.
     */
    public static ChatMessage.Message.Builder initializeMessage(SelectableChannel socketChannel) throws IOException {
        ChatMessage.Message.Builder messageBuilder = ChatMessage.Message.newBuilder();

        SocketChannel channel;
        ServerSocketChannel serverChannel;
        String localAddress = "", remoteAddress = "";

        if (socketChannel != null) {
            if (socketChannel instanceof SocketChannel) {
                // Represents a client channel
                channel = (SocketChannel) socketChannel;
                localAddress = channel.socket().getLocalSocketAddress().toString();
            } else {
                // Represents the server channel
                serverChannel = (ServerSocketChannel) socketChannel;
                localAddress = serverChannel.socket().getLocalSocketAddress().toString();
            }
        }

        messageBuilder.setSender(localAddress);

        return messageBuilder;
    }

    /**
     * Returns a string representation of a vector of Strings.
     *
     * @return
     */
    public static String getString(ConcurrentHashMap<String, String> hosts) {
        StringBuilder sb = new StringBuilder();

        for (String host : hosts.keySet()) {
            sb.append(host + "\t" + hosts.get(host) + "\n");
        }

        return sb.toString();
    }
}
