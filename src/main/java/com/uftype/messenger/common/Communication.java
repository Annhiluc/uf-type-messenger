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
    public static ChatMessage.Message buildMessage (String message, String username, SelectableChannel socketChannel,
                                                   ChatMessage.Message.ChatType chatType) throws IOException {
        ChatMessage.Message.Builder messageBuilder = initializeMessage(socketChannel);
        messageBuilder.setUsername(username);
        if (message != null) {
            messageBuilder.setText(message);
        }
        messageBuilder.setType(chatType);

        return messageBuilder.build();
    }

    /**
     * Build ChatMessage.Message as defined in ChatMessage.proto for communication.
     */
    public static ChatMessage.Message buildMessage (String message, byte[] file, String username, SelectableChannel socketChannel,
                                                    ChatMessage.Message.ChatType chatType) throws IOException {
        ChatMessage.Message.Builder messageBuilder = initializeMessage(socketChannel);
        messageBuilder.setUsername(username);
        if (message != null) {
            messageBuilder.setText(message);
        }
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
    public static ChatMessage.Message.Builder initializeMessage (SelectableChannel socketChannel) throws IOException{
        ChatMessage.Message.Builder messageBuilder = ChatMessage.Message.newBuilder();

        SocketChannel channel;
        ServerSocketChannel serverChannel;
        String localAddress = "", remoteAddress = "";

        if (socketChannel != null) {
            if (socketChannel instanceof SocketChannel) {
                // Represents a client channel
                channel = (SocketChannel) socketChannel;
                localAddress = channel.socket().getLocalSocketAddress().toString();
                remoteAddress = channel.socket().getRemoteSocketAddress().toString();
            }
            else {
                // Represents the server channel
                serverChannel = (ServerSocketChannel) socketChannel;
                localAddress = serverChannel.socket().getLocalSocketAddress().toString();
                remoteAddress = "ALL";
            }
        }

        messageBuilder.setSender(localAddress);
        messageBuilder.setRecipient(remoteAddress);

        return messageBuilder;
    }

    /**
     * Returns a string representation of a vector of Strings.
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
