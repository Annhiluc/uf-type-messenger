package com.uftype.messenger.gui;

import com.uftype.messenger.server.Server;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ServerGUI extends JFrame implements WindowListener, ActionListener {
    Server server;
    JTextArea chat, event;
    JButton start;

    public ServerGUI(Server server) {
        super("UF TYPE Messenger Server");
        this.server = server;

        // Add the start and stop buttons
        JPanel startPanel = new JPanel();
        start = new JButton("Start");
        start.addActionListener(this); // Will start the server
        startPanel.add(start);
        add(start, BorderLayout.NORTH);

        // Add the chat room
        JPanel chatPanel = new JPanel(new GridLayout(2,1));
        chat = new JTextArea(80, 80);
        chat.setEditable(false);
        chat.append("Chat room.\n");
        chat.setCaretPosition(chat.getText().length() - 1);
        chatPanel.add(new JScrollPane(chat));
        event = new JTextArea(80, 80);
        event.setEditable(false);
        event.append("Events log.\n");
        event.setCaretPosition(event.getText().length() - 1);
        chatPanel.add(new JScrollPane(event));
        add(chatPanel);

        // Add the file menu
        JMenuBar menuBar = new JMenuBar();
        JMenu menuFile = new JMenu("File");
        JMenuItem menuItemExit = new JMenuItem("Exit");
        menuFile.add(menuItemExit);
        menuBar.add(menuFile);

        // adds menu bar to the frame
        setJMenuBar(menuBar);

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
        int reply = JOptionPane.showConfirmDialog(ServerGUI.this,
                "Are you sure you want to quit?",
                "Exit",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (reply == JOptionPane.YES_OPTION) {
            // Stop server
            server = null;
            dispose();
            System.exit(0);
        } else {
            return;
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Stop server if it is running
        if (start.getText().equals("Stop")) {
            server = null;
            start.setText("Start");
            return;
        }
        else {
            // Start server here
            start.setText("Stop");
        }
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
