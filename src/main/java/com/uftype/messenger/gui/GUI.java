package com.uftype.messenger.gui;

import com.uftype.messenger.common.Dispatcher;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.concurrent.ConcurrentHashMap;

public abstract class GUI extends JFrame implements WindowListener, ActionListener {
    protected Dispatcher dispatcher;
    protected JPanel chatPanel, messagePanel;
    protected JLabel label;
    protected StyledDocument chatText, eventText;
    public JTextField messages;
    public JTextPane chat, event;

    public GUI(Dispatcher dispatcher, String name) {
        super(name);
        this.dispatcher = dispatcher;

        // Add the chat room
        chatPanel = new JPanel(new GridLayout(3, 1));
        chat = new JTextPane();
        chatPanel.add(new JScrollPane(chat));
        chatText = chat.getStyledDocument();
        addChat("Chat room.");

        // Add the chat room
        event = new JTextPane();
        chatPanel.add(new JScrollPane(event));
        eventText = event.getStyledDocument();
        addEvent("Events log.");
        add(chatPanel, BorderLayout.CENTER);

        // Add the file menu
        JMenuBar menuBar = new JMenuBar();
        JMenu menuFile = new JMenu("File");
        JMenuItem menuItemExit = new JMenuItem("Exit");
        menuFile.add(menuItemExit);
        menuBar.add(menuFile);
        // adds menu bar to the frame
        setJMenuBar(menuBar);

        // Add message text field
        messages = new JTextField("");
        label = new JLabel("Enter a chat message: ", SwingConstants.CENTER);

        messagePanel = new JPanel(new GridLayout(2, 1));
        messagePanel.add(label);
        messagePanel.add(messages);
        chatPanel.add(messagePanel);

        messages.addActionListener(this);
        addWindowListener(this);

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE); // Will not close unless prompted
    }

    public void loadScreen() {
        // Size the frame.
        //frame.pack();
        setSize(1000, 750);

        // Set the frame icon to an image loaded from a file.
        setIconImage(new ImageIcon(getClass().getClassLoader().getResource("type-icon.png")).getImage());

        // Show it.
        setVisible(true);
    }

    @Override
    public void windowClosing(WindowEvent e) {
        int reply = JOptionPane.showConfirmDialog(GUI.this,
                "Are you sure you want to quit?",
                "Exit",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (reply == JOptionPane.YES_OPTION) {
            // Stop dispatcher
            dispatcher.stop();
            dispose();
            System.exit(0);
        } else {
            return;
        }
    }

    public void addChat(String message) {
        try {
            chatText.insertString(chatText.getLength(), message + "\n", null);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    public void addEvent(String message) {
        try {
            eventText.insertString(eventText.getLength(), message + "\n", null);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates the screen showing new logged in users.
     *
     * @param users
     */
    public abstract void updateUsers(ConcurrentHashMap<String, String> users);

    @Override
    public void windowOpened(WindowEvent e) {

    }

    @Override
    public void windowClosed(WindowEvent e) {

    }

    @Override
    public void windowIconified(WindowEvent e) {

    }

    @Override
    public void windowDeiconified(WindowEvent e) {

    }

    @Override
    public void windowActivated(WindowEvent e) {

    }

    @Override
    public void windowDeactivated(WindowEvent e) {

    }
}
