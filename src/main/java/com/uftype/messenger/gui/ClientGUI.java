package com.uftype.messenger.gui;

import com.uftype.messenger.client.Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ClientGUI extends JFrame implements WindowListener, ActionListener {
    public Client client;
    public JTextArea chat;
    public JButton login, logout, start;
    private JLabel label;
    private JTextField messages;


    public ClientGUI(Client client) {
        super("UF TYPE Messenger Client");
        this.client = client;

        // Add the start and stop buttons
        JPanel startPanel = new JPanel();
        start = new JButton("Start");
        start.addActionListener(this); // Will start the client
        startPanel.add(start);
        add(start, BorderLayout.NORTH);

        // Add the chat room
        JPanel chatPanel = new JPanel(new GridLayout(1,1));
        chat = new JTextArea(80, 80);
        chat.setEditable(false);
        chat.append("Chat room.\n");
        chat.setCaretPosition(chat.getText().length() - 1);
        chatPanel.add(new JScrollPane(chat));
        add(chatPanel);

        // Add the file menu
        JMenuBar menuBar = new JMenuBar();
        JMenu menuFile = new JMenu("File");
        JMenuItem menuItemExit = new JMenuItem("Exit");
        menuFile.add(menuItemExit);
        menuBar.add(menuFile);

        // adds menu bar to the frame
        setJMenuBar(menuBar);

        // Add login and logout button
        login = new JButton("Login");
        login.addActionListener(this);
        logout = new JButton("Logout");
        logout.addActionListener(this);
        logout.setEnabled(false);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(login);
        buttonPanel.add(logout);
        add(buttonPanel, BorderLayout.SOUTH);

        // Add message text field
        messages = new JTextField("");
        label = new JLabel("Enter a chat message: ", SwingConstants.CENTER);

        JPanel northPanel = new JPanel(new GridLayout(2,1));
        northPanel.add(label);
        northPanel.add(messages);
        add(northPanel, BorderLayout.NORTH);

        messages.addActionListener(this);
        addWindowListener(this);

        // Size the frame.
        //frame.pack();
        setSize(1000,750);

        // Set the frame icon to an image loaded from a file.
        setIconImage(new ImageIcon("src/main/resources/type-icon.png").getImage());

        // Show it.
        setVisible(true);
    }

    @Override
    public void windowOpened(WindowEvent e) {

    }

    @Override
    public void windowClosing(WindowEvent e) {
        int reply = JOptionPane.showConfirmDialog(ClientGUI.this,
                "Are you sure you want to quit?",
                "Exit",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (reply == JOptionPane.YES_OPTION) {
            // Stop client
            dispose();
            System.exit(0);
        } else {
            return;
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();

        if (o == login) {

        }
        else if (o == logout) {

        }
        else if (o == messages){
            // Need to send message

            addChat(messages.getText());
            messages.setText(""); // Clear message
        }
    }

    public void addChat(String message) {
        chat.append(message + "\n");
        chat.setCaretPosition(chat.getText().length() - 1);
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
