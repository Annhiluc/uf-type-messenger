package com.uftype.messenger.gui;

import com.uftype.messenger.common.Communication;
import com.uftype.messenger.common.Dispatcher;
import com.uftype.messenger.proto.ChatMessage;
import com.uftype.messenger.server.ServerDispatcher;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.concurrent.ConcurrentHashMap;

public class ServerGUI extends GUI {
    public JButton start;

    public ServerGUI(Dispatcher serverDispatcher) {
        super(serverDispatcher, "UF TYPE Messenger Server");

        // Add the start and stop buttons
        JPanel startPanel = new JPanel();
        start = new JButton("Stop");
        start.setFont(monoFont);
        start.addActionListener(this); // Will start the server
        startPanel.add(start);
        add(start, BorderLayout.NORTH);

        loadScreen();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();
        // Stop server if it is running
        if (o == start) {
            if (start.getText().equals("Stop")) {
                // Need to alert other clients
                try {
                    SelectionKey key = dispatcher.channel.keyFor(dispatcher.selector);
                    // Build and attach message
                    ChatMessage.Message chatMessage = Communication.buildMessage(
                            "", dispatcher.username, "ALL",
                            key.channel(), ChatMessage.Message.ChatType.LOGOUT);
                    key.attach(ByteBuffer.wrap(chatMessage.toByteArray()));
                    ((ServerDispatcher) dispatcher).doWriteAll(key);
                } catch (IOException err) {
                    err.printStackTrace();
                }

                dispatcher.stop();
                dispose();
                System.exit(0);
                return;
            }
        } else if (o == messages && !messages.getText().equals("")) {
            try {
                // Get the associated key to attach message
                SelectionKey key = dispatcher.channel.keyFor(dispatcher.selector);
                // Build and attach message
                ChatMessage.Message chatMessage = Communication.buildMessage(
                        messages.getText(), dispatcher.username, "ALL",
                        key.channel(), ChatMessage.Message.ChatType.TEXT);
                key.attach(ByteBuffer.wrap(chatMessage.toByteArray()));
                ((ServerDispatcher) dispatcher).doWriteAll(key);

                addChat(dispatcher.username + ": " + messages.getText());
                messages.setText(""); // Clear message
            } catch (IOException err) {
                err.printStackTrace();
            }
        }
    }

    @Override
    public void updateUsers(ConcurrentHashMap<String, String> users) {

    }
}
