package com.uftype.messenger.gui;

import com.uftype.messenger.common.Dispatcher;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public abstract class GUI extends JFrame implements WindowListener, ActionListener {
    protected Dispatcher dispatcher;
    protected JTextArea chat, event;
    protected JPanel chatPanel, messagePanel;
    protected JLabel label;
    public JTextField messages;

    public GUI (Dispatcher dispatcher, String name) {
        super(name);
        this.dispatcher = dispatcher;

        // Add the chat room
        chatPanel = new JPanel(new GridLayout(2,1));
        chat = new JTextArea(80, 80);
        chat.setEditable(false);
        addChat("Chat room.");
        chatPanel.add(new JScrollPane(chat));

        // Add the chat room
        event = new JTextArea(80, 80);
        event.setEditable(false);
        addEvent("Events log.");
        chatPanel.add(new JScrollPane(event));
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

        messagePanel = new JPanel(new GridLayout(2,1));
        messagePanel.add(label);
        messagePanel.add(messages);
        add(messagePanel, BorderLayout.SOUTH);

        messages.addActionListener(this);
        addWindowListener(this);
    }

    public void loadScreen() {
        // Size the frame.
        //frame.pack();
        setSize(1000,750);

        // Set the frame icon to an image loaded from a file.
        setIconImage(new ImageIcon("src/main/resources/type-icon.png").getImage());

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
        chat.append(message + "\n");
        chat.setCaretPosition(chat.getText().length() - 1);
    }

    public void addEvent(String message) {
        event.append(message + "\n");
        event.setCaretPosition(event.getText().length() - 1);
    }

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
