package com.uftype.messenger.gui;

import com.uftype.messenger.auth.Authentication;
import com.uftype.messenger.common.Communication;
import com.uftype.messenger.common.Dispatcher;
import com.uftype.messenger.proto.ChatMessage;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.TextEditorPane;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class ClientGUI extends GUI {
    private JButton sendCode;         // Buttons to represent logout, adding a file, and sending sendCode
    private RSyntaxTextArea textArea;           // Syntax text box for sendCode
    private ConcurrentHashMap<JButton, String> users; // Maps between other users and their hostnames
    private JPanel otherUsers;                  // Panel showing buttons of other users
    private JComboBox languageList;             // Language list to choose language
    private JPanel chatScreen, codeScreen;      // Screen to hold the chat and sendCode windows

    public Login login;                         // Login screen
    private JButton chat, code, file, logout;

    //Create a file chooser
    final private JFileChooser fc = new JFileChooser();

    public ClientGUI(Dispatcher clientDispatcher) {
        super(clientDispatcher, "UF TYPE Messenger Client");

        users = new ConcurrentHashMap<>();

        // Label to demonstrate for other users
        JLabel other = new JLabel("<html>Type a message and click on another user to send them a private message!<html>");
        other.setFont(monoFont);
        other.setForeground(Color.white);
        other.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
        otherUsers = new JPanel();
        otherUsers.add(other);
        otherUsers.setBackground(blue);

        JPanel menu = new JPanel(new GridLayout(5, 1));

        URL resource = getClass().getClassLoader().getResource("type-logo.png");
        if (resource != null) {
            ImageIcon ii = new ImageIcon(resource);
            JLabel label = new JLabel(ii);
            menu.add(label);
        }

        chat = new JButton("Chat");
        chat.setFont(monoFont);
        chat.setBackground(orange);
        chat.addChangeListener(orangeBtn);
        chat.addActionListener(this);
        menu.add(chat);

        code = new JButton("Code");
        code.setFont(monoFont);
        code.setBackground(orange);
        code.addChangeListener(orangeBtn);
        code.addActionListener(this);
        menu.add(code);

        file = new JButton("Add File");
        file.setFont(monoFont);
        file.setBackground(orange);
        file.addChangeListener(orangeBtn);
        file.addActionListener(this);
        menu.add(file);

        logout = new JButton("Logout");
        logout.setFont(monoFont);
        logout.setBackground(orange);
        logout.addChangeListener(orangeBtn);
        logout.addActionListener(this);
        menu.add(logout);

        //menu.setBorder(BorderFactory.createEmptyBorder(15, 15, 0, 15));
        menu.setBackground(blue);

        // Add sendCode syntax text pane
        textArea = new TextEditorPane(TextEditorPane.INSERT_MODE);
        textArea.setFont(textArea.getFont().deriveFont(24.0f));
        JPanel cp = new JPanel(new GridLayout(3, 1));
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
        textArea.setCodeFoldingEnabled(true);
        RTextScrollPane sp = new RTextScrollPane(textArea);

        // Add languages for sendCode syntax
        String[] languages = {"Java", "JavaScript", "C", "C++", "C#", "JSON", "HTML", "CSS", "Python"};
        languageList = new JComboBox<>(languages);
        languageList.setFont(monoFont);
        languageList.setBackground(blue);
        languageList.setForeground(Color.white);
        languageList.addActionListener(this);
        cp.add(languageList);
        cp.add(sp);

        // Button to send sendCode to another user
        sendCode = new JButton("Send Code");
        sendCode.setFont(monoFont);
        sendCode.setBackground(beige);
        sendCode.addChangeListener(yellowBtn);
        sendCode.addActionListener(this);
        cp.add(sendCode);

        // Add to the screen all the components
        chatScreen = new JPanel(new GridLayout(1, 2));
        chatScreen.add(chatPanel, BorderLayout.CENTER);
        chatScreen.add(otherUsers, BorderLayout.EAST);

        add(messagePanel, BorderLayout.SOUTH);
        // Add both menu and chatscreen to screen
        //JPanel screen = new JPanel(new GridLayout(1, 2));
        add(menu, BorderLayout.WEST);
        add(chatScreen, BorderLayout.CENTER);
        pack();

        codeScreen = new JPanel(new GridLayout(1, 2));
        codeScreen.add(cp);

        // Make a login/register opening frame, and when it successfully authenticates, load frame
        login = new Login(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();

        if (o == logout) {
            // Create label to prompt when users try to close.
            JLabel label = new JLabel("Are you sure you want to quit?");
            label.setFont(monoFont);

            // Open confirm dialog with label
            int reply = JOptionPane.showConfirmDialog(ClientGUI.this,
                    label,
                    "Exit",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (reply == JOptionPane.YES_OPTION) {
                // Alert server of logout
                if (Authentication.logout(dispatcher.username)) {
                    // Send logout message to the server
                    // Build and attach message with username
                    SelectionKey key = dispatcher.channel.keyFor(dispatcher.selector);
                    try {
                        ChatMessage.Message chatMessage = Communication.buildMessage(
                                "", dispatcher.username, "ALL",
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
        } else if (o == sendCode && !textArea.getText().equals("")) {
            try {
                // Get the associated key to attach message
                SelectionKey key = dispatcher.channel.keyFor(dispatcher.selector);
                // Build and attach message
                ChatMessage.Message chatMessage = Communication.buildCodeMessage(textArea.getText(),
                        dispatcher.username, "ALL", (String) languageList.getSelectedItem(),
                        key.channel(), ChatMessage.Message.ChatType.CODE);
                key.attach(ByteBuffer.wrap(chatMessage.toByteArray()));
                dispatcher.doWrite(key);

                addEvent(dispatcher.username + ": sent sendCode snippet");
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
            // Change RSyntaxPane sendCode for syntax highlighting.
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
        } else if (o == chat) {
            remove(codeScreen);
            add(chatScreen, BorderLayout.CENTER);
            validate();
            repaint(); // Automatically update the screen
        } else if (o == code) {
            remove(chatScreen);
            add(codeScreen, BorderLayout.CENTER);
            validate();
            repaint(); // Automatically update the screen
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
                user.addChangeListener(yellowBtn);
                user.setBackground(beige);
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
