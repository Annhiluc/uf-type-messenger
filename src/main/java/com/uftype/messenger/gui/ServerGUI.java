package com.uftype.messenger.gui;

import com.uftype.messenger.common.Communication;
import com.uftype.messenger.common.Dispatcher;
import com.uftype.messenger.proto.ChatMessage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

public class ServerGUI extends GUI {
    public JButton start;

    public ServerGUI(Dispatcher serverDispatcher) {
        super(serverDispatcher, "UF TYPE Messenger Server");

        // Add the start and stop buttons
        JPanel startPanel = new JPanel();
        start = new JButton("Start");
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
                dispatcher.stop();
                start.setText("Start");
                return;
            }
            else {
                // Start dispatcher
                start.setText("Stop");
            }
        }
        else if (o == messages && !messages.getText().equals("")){
            try {
                // Get the associated key to attach message
                SelectionKey key = dispatcher.channel.keyFor(dispatcher.selector);
                // Build and attach message
                ChatMessage.Message chatMessage = Communication.buildMessage(messages.getText(), dispatcher.username,
                        key.channel(), ChatMessage.Message.ChatType.TEXT);
                key.attach(ByteBuffer.wrap(chatMessage.toByteArray()));
                dispatcher.doWrite(key);

                addChat(dispatcher.username + ": " + messages.getText());
                messages.setText(""); // Clear message
            } catch (IOException err) {
                err.printStackTrace();
            }
        }
    }
}
