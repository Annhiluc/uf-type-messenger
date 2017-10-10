package com.uftype.messenger.common;

import com.uftype.messenger.proto.ChatMessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents the receiver which is employed by client and server to facilitate message sending and receiving.
 * Is only used in console version of application.
 */
public class Receiver extends Thread {
    protected Dispatcher dispatcher; // Dispatcher to connect channel and selectors

    protected final Logger LOGGER = Logger.getLogger(Receiver.class.getName());

    public Receiver(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    /**
     * Receive messages written to System.in and perform write operation on them.
     */
    public void run() {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        try {
            while (dispatcher.isUp) {
                String message = in.readLine();

                // Get the associated key to attach message
                SelectionKey key = dispatcher.channel.keyFor(dispatcher.selector);

                // Build and attach message
                ChatMessage.Message chatMessage = Communication.buildMessage(message, dispatcher.username, "ALL",
                        key.channel(), ChatMessage.Message.ChatType.TEXT);
                key.attach(ByteBuffer.wrap(chatMessage.toByteArray()));
                dispatcher.doWrite(key);
                //key.interestOps(OP_WRITE);
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "UF TYPE receiving messages failure: " + e);
        }

        try {
            in.close();
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "UF TYPE failure in closing buffered reader: " + e);
        }
    }
}
