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

public class ClientGUI extends GUI {
    public JButton logout, file, code;
    public Login login;
    protected RSyntaxTextArea textArea;
    protected ConcurrentHashMap<JButton, String> users; // Maps between other users and their hostnames
    protected JPanel otherUsers, screen;
    protected JComboBox languageList;

    //Create a file chooser
    final JFileChooser fc = new JFileChooser();

    public ClientGUI(Dispatcher clientDispatcher) {
        super(clientDispatcher, "UF TYPE Messenger Client");

        users = new ConcurrentHashMap<JButton, String>();

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

        screen = new JPanel(new GridLayout(1, 3));

        textArea = new RSyntaxTextArea();
        JPanel cp = new JPanel(new GridLayout(3, 1));
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT);
        textArea.setCodeFoldingEnabled(true);
        RTextScrollPane sp = new RTextScrollPane(textArea);

        String[] languages = {"Java", "JavaScript", "C", "C++", "C#", "JSON", "HTML", "CSS", "Python"};
        languageList = new JComboBox(languages);
        languageList.addActionListener(this);
        cp.add(languageList);

        cp.add(sp);

        code = new JButton("Send Code");
        code.addActionListener(this);
        cp.add(code);

        otherUsers = new JPanel();

        screen.add(chatPanel, BorderLayout.WEST);
        screen.add(cp, BorderLayout.CENTER);
        screen.add(otherUsers, BorderLayout.EAST);
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
                dispatcher.stop();
                dispose();
                System.exit(0);
            } else {
                // Something failed, quit application
                System.out.println("Error logging out of the application.");
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
                err.printStackTrace();
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
                err.printStackTrace();
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
                    bis.read(mybytearray, 0, mybytearray.length);

                    SelectionKey key = dispatcher.channel.keyFor(dispatcher.selector);

                    // Handle files which are > 14000 bits
                    if (myFile.length() > 14000) {
                        addEvent("Please send files that are less than 14KB.");
                        return;
                    }

                    // Send file message
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
                err.printStackTrace();
            }
        } else if (o == languageList) {
            JComboBox cb = (JComboBox) o;
            String language = (String) cb.getSelectedItem();
            switch (language) {
                case "Java":
                    textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
                    revalidate();
                    repaint();
                    break;
                case "JavaScript":
                    textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT);
                    revalidate();
                    repaint();
                    break;
                case "C":
                    textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_C);
                    revalidate();
                    repaint();
                    break;
                case "C++":
                    textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_CSHARP);
                    revalidate();
                    repaint();
                    break;
                case "C#":
                    textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_CPLUSPLUS);
                    revalidate();
                    repaint();
                    break;
                case "JSON":
                    textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JSON);
                    revalidate();
                    repaint();
                    break;
                case "HTML":
                    textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_HTML);
                    revalidate();
                    repaint();
                    break;
                case "CSS":
                    textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_CSS);
                    revalidate();
                    repaint();
                    break;
                case "Python":
                    textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_PYTHON);
                    revalidate();
                    repaint();
                    break;
            }
        } else if (users.containsKey(o) && !messages.getText().equals("")) {
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

    @Override
    public void updateUsers(ConcurrentHashMap<String, String> hosts) {
        otherUsers.setLayout(new GridLayout(hosts.size(), 1));

        // Remove hosts that have disconnected
        for (JButton button : users.keySet()) {
            if (!hosts.containsValue(button.getText())) {
                otherUsers.remove(button);
            }
        }

        // Add new hosts not contained already
        for (String host : hosts.keySet()) {
            if (!users.containsValue(host)) {
                // Need to populate with currently logged in users
                JButton user = new JButton(dispatcher.connectedHosts.get(host));
                user.addActionListener(this); // To create a chat window with that one user
                users.put(user, host);

                otherUsers.add(user);
            }
        }

        validate();
        repaint(); // Automatically update the screen
        setVisible(true);
    }

    @Override
    public void addChat(String message) {
        super.addChat(message);

        // Play a sound when something comes in
        try {
            String soundName = "src/main/resources/notification.wav";
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(soundName).getAbsoluteFile());
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();
        } catch (UnsupportedAudioFileException e) {
            System.out.println("Unable to read file type.");
        } catch (IOException e) {
            System.out.println("Unable to find file.");
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
