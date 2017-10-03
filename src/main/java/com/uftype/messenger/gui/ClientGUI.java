package com.uftype.messenger.gui;

import com.uftype.messenger.common.Communication;
import com.uftype.messenger.common.Dispatcher;
import com.uftype.messenger.proto.ChatMessage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

public class ClientGUI extends GUI {
    public JButton logout, file;
    protected Login login;
    //Create a file chooser
    final JFileChooser fc = new JFileChooser();

    public ClientGUI(Dispatcher clientDispatcher) {
        super(clientDispatcher, "UF TYPE Messenger Client");

        // Add login and logout button
        logout = new JButton("Logout");
        logout.addActionListener(this);
        file = new JButton("Add File");
        file.addActionListener(this);
        logout.setEnabled(true);
        file.setEnabled(true);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(logout);
        buttonPanel.add(file);
        add(buttonPanel, BorderLayout.NORTH);

        // Make a login/register opening frame, and when it successfully authenticates, load frame
        login = new Login(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();

        if (o == logout) {
            // Do logout procedures

            logout.setEnabled(false);
            file.setEnabled(false);

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
        else if (o == file) {
            // Transfer file here
            try {
                //In response to a button click:
                int returnVal = fc.showOpenDialog(ClientGUI.this);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    // Send file
                    File myFile = fc.getSelectedFile();
                    byte [] mybytearray  = new byte [(int)myFile.length()];
                    BufferedInputStream bis = new BufferedInputStream(new FileInputStream(myFile));
                    bis.read(mybytearray, 0, mybytearray.length);

                    SelectionKey key = dispatcher.channel.keyFor(dispatcher.selector);

                    // Handle files which are > 14000 bits
                    if (myFile.length() > 14000) {
                        addEvent("Please send files that are less than 14KB.");
                        return;
                    }

                    // Send file message
                    ChatMessage.Message fileRequest = Communication.buildMessage(myFile.getName(), mybytearray,
                            dispatcher.username, key.channel(), ChatMessage.Message.ChatType.FILE);

                    // Write message here
                    key.attach(ByteBuffer.wrap(fileRequest.toByteArray()));
                    dispatcher.doWrite(key);

                    // If image, put it in the chat message
                    if (myFile.getName().endsWith("jpg") || myFile.getName().endsWith("png")) {
                        addEvent(dispatcher.username + ": ");
                        event.insertIcon(new ImageIcon(myFile.getAbsolutePath()));
                        addEvent("\n"); // Makes it on a new line
                    }

                    // Close input stream
                    bis.close();
                } else if (returnVal != JFileChooser.CANCEL_OPTION) {
                    addEvent("Unable to choose that file. Please try again.");
                }
            } catch (IOException err) {
                err.printStackTrace();
            }
        }
    }

    @Override
    public void windowOpened(WindowEvent e) {
        // Set the username since the user is logged in
        dispatcher.username = login.loggedInUser.username;
    }
}
