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

public class ClientGUI extends GUI {
    public JButton login, logout;
    private boolean loggedIn;

    public ClientGUI(Dispatcher clientDispatcher) {
        super(clientDispatcher, "UF TYPE Messenger Client");
        loggedIn = false;

        // Add login and logout button
        login = new JButton("Login");
        login.addActionListener(this);
        logout = new JButton("Logout");
        logout.addActionListener(this);
        logout.setEnabled(false);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(login);
        buttonPanel.add(logout);
        add(buttonPanel, BorderLayout.NORTH);

        label.setText("Please enter a username: ");

        loadScreen();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();

        if (!loggedIn) {
            String username = messages.getText().trim();
            if (username.equals("")) {
                // Prompt user to enter username
                JOptionPane.showMessageDialog(ClientGUI.this, "Please provide a username.");
                return;
            }

            dispatcher.username = username;

            // Can do login and authentication here

            label.setText("Enter a chat message: ");
            messages.setText("");

            login.setEnabled(false);
            logout.setEnabled(true);
            loggedIn = true;
        }
        else if (o == logout && loggedIn) {

        }
        else if (o == messages && !messages.getText().equals("") && loggedIn){
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
