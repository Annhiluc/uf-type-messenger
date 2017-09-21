package com.uftype.messenger.gui;

import com.uftype.messenger.server.Server;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

public class ServerGUI extends JFrame implements WindowListener, ActionListener {
    public Server server;
    public JTextArea chat, event;
    public JButton start;

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
        addChat("Chat room.");
        chatPanel.add(new JScrollPane(chat));
        event = new JTextArea(80, 80);
        event.setEditable(false);
        addEvent("Events log.");
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
            server.disconnect();
            server = null;
            start.setText("Start");
            return;
        }
        else {
            //try {
                start.setText("Stop");
            //} catch (IOException err) {
              //  err.printStackTrace();
            //}
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
