package com.uftype.messenger.gui;

import com.uftype.messenger.auth.Authentication;
import com.uftype.messenger.common.Communication;
import com.uftype.messenger.common.Dispatcher;
import com.uftype.messenger.proto.ChatMessage;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class ClientGUI extends GUI {
    private JButton logout, file, code;         // Buttons to represent logout, adding a file, and sending code
    private RSyntaxTextArea textArea;           // Syntax text box for code
    private ConcurrentHashMap<JButton, String> users; // Maps between other users and their hostnames
    private JPanel otherUsers;                  // Panel showing buttons of other users
    private JComboBox languageList;             // Language list to choose language

    public Login login;                         // Login screen

    //Create a file chooser
    final private JFileChooser fc = new JFileChooser();

    public ClientGUI(Dispatcher clientDispatcher) {
        super(clientDispatcher, "UF TYPE Messenger Client");

        users = new ConcurrentHashMap<>();

        // Add login and logout button
        JPanel buttonPanel = new JPanel();
        logout = new JButton("Logout");
        logout.setFont(monoFont);
        logout.addActionListener(this);
        file = new JButton("Add File");
        file.setFont(monoFont);
        file.addActionListener(this);
        logout.setEnabled(true);
        file.setEnabled(true);
        buttonPanel.add(logout);
        buttonPanel.add(file);
        add(buttonPanel, BorderLayout.NORTH);

        // Label to demonstrate for other users
        JLabel other = new JLabel("<html>Click on another user to send them a private message!<html>");
        other.setFont(monoFont);
        otherUsers = new JPanel();
        otherUsers.add(other);

        // Add code syntax text pane
        textArea = new RSyntaxTextArea();
        textArea.setFont(textArea.getFont().deriveFont(24.0f));
        JPanel cp = new JPanel(new GridLayout(3, 1));
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT);
        textArea.setCodeFoldingEnabled(true);
        RTextScrollPane sp = new RTextScrollPane(textArea);

        // Add languages for code syntax
        String[] languages = {"Java", "JavaScript", "C", "C++", "C#", "JSON", "HTML", "CSS", "Python"};
        languageList = new JComboBox<>(languages);
        languageList.setFont(monoFont);
        languageList.addActionListener(this);
        cp.add(languageList);
        cp.add(sp);

        // Button to send code to another user
        code = new JButton("Send Code");
        code.setFont(monoFont);
        code.addActionListener(this);
        cp.add(code);

        // Add to the screen all the components
        JPanel screen = new JPanel(new GridLayout(1, 3));
        screen.add(chatPanel, BorderLayout.WEST);
        screen.add(otherUsers, BorderLayout.CENTER);
        screen.add(cp, BorderLayout.EAST);
        add(screen, BorderLayout.CENTER);

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

            if (Authentication.logout(dispatcher.username)) {
                // Send logout message to the server
                // Build and attach message with username
                SelectionKey key = dispatcher.channel.keyFor(dispatcher.selector);
                try {
                    ChatMessage.Message chatMessage = Communication.buildMessage("", dispatcher.username, "ALL",
                            key.channel(), ChatMessage.Message.ChatType.LOGOUT);
                    key.attach(ByteBuffer.wrap(chatMessage.toByteArray()));
                    dispatcher.doWrite(key);
                } catch (IOException err) {
                    logger.log(Level.WARNING, err.toString());
                }

                dispatcher.stop();
                dispose();
                System.exit(0);
            } else {
                // Something failed, quit application
                logger.log(Level.WARNING, "Error logging out of the application.");
                dispatcher.stop();
                dispose();
                System.exit(1);
            }
        } else if (o == messages && !messages.getText().equals("")) {
            try {
                // Get the associated key to attach message
                SelectionKey key = dispatcher.channel.keyFor(dispatcher.selector);
                // Build and attach message
                ChatMessage.Message chatMessage = Communication.buildMessage(messages.getText(),
                        dispatcher.username, "ALL", key.channel(), ChatMessage.Message.ChatType.TEXT);
                key.attach(ByteBuffer.wrap(chatMessage.toByteArray()));
                dispatcher.doWrite(key);

                addChat(dispatcher.username + ": " + messages.getText());
                messages.setText(""); // Clear message
            } catch (IOException err) {
                logger.log(Level.WARNING, err.toString());
            }
        } else if (o == code && !textArea.getText().equals("")) {
            try {
                // Get the associated key to attach message
                SelectionKey key = dispatcher.channel.keyFor(dispatcher.selector);
                // Build and attach message
                ChatMessage.Message chatMessage = Communication.buildCodeMessage(textArea.getText(),
                        dispatcher.username, "ALL", (String) languageList.getSelectedItem(),
                        key.channel(), ChatMessage.Message.ChatType.CODE);
                key.attach(ByteBuffer.wrap(chatMessage.toByteArray()));
                dispatcher.doWrite(key);

                addEvent(dispatcher.username + ": sent code snippet");
                textArea.setText(""); // Clear message
            } catch (IOException err) {
                logger.log(Level.WARNING, err.toString());
            }
        } else if (o == file) {
            // Transfer file here
            try {
                //In response to a button click:
                int returnVal = fc.showOpenDialog(ClientGUI.this);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    // Send file
                    File myFile = fc.getSelectedFile();

                    byte[] mybytearray = new byte[(int) myFile.length()];
                    BufferedInputStream bis = new BufferedInputStream(new FileInputStream(myFile));
                    int read = bis.read(mybytearray, 0, mybytearray.length);

                    // Handle files which are > 14000 bits
                    if (read < 0 || myFile.length() > 14000) {
                        addEvent("Please send files that are less than 14KB.");
                        return; // This read operation failed
                    }

                    // Send file message
                    SelectionKey key = dispatcher.channel.keyFor(dispatcher.selector);
                    ChatMessage.Message fileRequest = Communication.buildMessage(myFile.getName(), mybytearray,
                            "ALL", dispatcher.username, null, key.channel(),
                            ChatMessage.Message.ChatType.FILE);

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
                logger.log(Level.WARNING, err.toString());
            }
        } else if (o == languageList) {
            // Change RSyntaxPane code for syntax highlighting.
            JComboBox cb = (JComboBox) o;
            String language = cb.getSelectedItem() == null ? "Java" : (String) cb.getSelectedItem();
            switch (language) {
                case "Java":
                    textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
                    break;
                case "JavaScript":
                    textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT);
                    break;
                case "C":
                    textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_C);
                    break;
                case "C++":
                    textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_CSHARP);
                    break;
                case "C#":
                    textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_CPLUSPLUS);
                    break;
                case "JSON":
                    textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JSON);
                    break;
                case "HTML":
                    textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_HTML);
                    break;
                case "CSS":
                    textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_CSS);
                    break;
                case "Python":
                    textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_PYTHON);
                    break;
                default:
                    break;
            }

            revalidate();
            repaint();
        } else if (o instanceof JButton && users.containsKey(o) && !messages.getText().equals("")) {
            // Need to send specific message
            try {
                // Get the associated key to attach message
                SelectionKey key = dispatcher.channel.keyFor(dispatcher.selector);
                // Build and attach message
                ChatMessage.Message chatMessage = Communication.buildMessage(messages.getText(),
                        dispatcher.username, users.get(o),
                        key.channel(), ChatMessage.Message.ChatType.TEXT);
                key.attach(ByteBuffer.wrap(chatMessage.toByteArray()));
                dispatcher.doWrite(key);

                addChat("PRIVATE MESSAGE to " + users.get(o) + " from " + dispatcher.username + ": " +
                        messages.getText());
                messages.setText(""); // Clear message
            } catch (IOException err) {
                err.printStackTrace();
            }
        }
    }

    /**
     * Update the users showing in the user panel.
     */
    @Override
    public void updateUsers(ConcurrentHashMap<String, String> hosts) {
        otherUsers.setLayout(new GridLayout(hosts.size() + 1, 1));

        // Remove hosts that have disconnected
        for (JButton button : users.keySet()) {
            if (!hosts.containsKey(users.get(button))) {
                otherUsers.remove(button);
            }
        }

        // Add new hosts not contained already
        for (String host : hosts.keySet()) {
            if (!users.containsValue(host)) {
                // Need to populate with currently logged in users
                JButton user = new JButton(dispatcher.connectedHosts.get(host));
                user.setFont(monoFont);
                user.addActionListener(this); // To create a chat window with that one user
                users.put(user, host);

                otherUsers.add(user);
            }
        }

        validate();
        repaint(); // Automatically update the screen
        setVisible(true);
    }

    /**
     * When a chat gets added to the chat panel, play an audio sound.
     */
    @Override
    public void addChat(String message) {
        super.addChat(message);

        // Play a sound when something comes in
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(
                    getClass().getClassLoader().getResourceAsStream("notification.wav"));
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();
        } catch (UnsupportedAudioFileException e) {
            logger.log(Level.WARNING, "Unable to read file type.");
        } catch (IOException e) {
            logger.log(Level.WARNING, "Unable to find file.");
        } catch (LineUnavailableException e) {
            // Do nothing
        }
    }

    @Override
    public void windowOpened(WindowEvent e) {
        // Set the username since the user is logged in
        dispatcher.username = login.loggedInUser.username;
    }
}
