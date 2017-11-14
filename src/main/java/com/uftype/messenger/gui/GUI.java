package com.uftype.messenger.gui;

import com.uftype.messenger.common.Communication;
import com.uftype.messenger.common.Dispatcher;
import com.uftype.messenger.proto.ChatMessage;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class GUI extends JFrame implements WindowListener, ActionListener {
    private StyledDocument chatText, eventText;   // Text documents holding text for event and chat panels
    protected Logger logger = Logger.getLogger(GUI.class.getName());   // Logger to keep track of GUI events

    Dispatcher dispatcher;          // Dispatcher (either Client or Server) to handle writing messages
    JPanel messagePanel, chatPanel; // Panel to show the chat and message panel
    JTextField messages;            // Textfield to input messages
    JTextPane event;                // Event pane to show event messages
    Font monoFont = new Font(Font.DIALOG_INPUT, Font.BOLD, 24);   // Special font

    OrangeChangeListener orangeBtn = new OrangeChangeListener();
    YellowChangeListener yellowBtn = new YellowChangeListener();
    Color blue = new Color(13, 59, 102);
    Color orange = new Color(228, 150, 75);
    private Color yellow = new Color(244, 211, 94);
    Color beige = new Color(250, 240, 202);

    GUI(Dispatcher dispatcher, String name) {
        super(name);
        this.dispatcher = dispatcher;

        // Add the chat room
        messagePanel = new JPanel(new GridLayout(3, 1));
        chatPanel = new JPanel(new GridLayout(1, 1));
        JTextPane chat = new JTextPane();
        chat.setEditable(false);
        chatPanel.add(new JScrollPane(chat), BorderLayout.CENTER);
        chatText = chat.getStyledDocument();
        chat.setFont(monoFont);
        addChat("Chat room.");

        // Add message text panel
        messages = new JTextField();
        messages.setFont(monoFont);
        JLabel messageLabel = new JLabel("Enter a chat message: ", SwingConstants.CENTER);
        messageLabel.setFont(monoFont);
        messageLabel.setForeground(Color.white);
        messagePanel.add(messageLabel);
        messagePanel.add(messages);
        messagePanel.setBackground(blue);

        // Add the event pane
        event = new JTextPane();
        eventText = event.getStyledDocument();
        event.setFont(monoFont);
        addEvent("Events log.");
        event.setEditable(false);
        messagePanel.add(new JScrollPane(event));
        add(messagePanel, BorderLayout.SOUTH);
        add(chatPanel, BorderLayout.CENTER);

        // Add action listeners for message text field and frame
        messages.addActionListener(this);
        addWindowListener(this);

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE); // Will not close unless prompted
    }

    /**
     * Load the screen.
     */
    void loadScreen() {
        // Size the frame.
        setSize(1750, 1500);

        // Set the frame icon to an image loaded from a file.
        URL resource = getClass().getClassLoader().getResource("type-icon.png");

        if (resource != null) {
            setIconImage(new ImageIcon(resource).getImage());
        }

        // Show it.
        setVisible(true);
    }

    /**
     * Add label to verify exit.
     */
    @Override
    public void windowClosing(WindowEvent e) {
        // Create label to prompt when users try to close.
        JLabel label = new JLabel("Are you sure you want to quit?");
        label.setFont(monoFont);

        // Open confirm dialog with label
        int reply = JOptionPane.showConfirmDialog(GUI.this,
                label,
                "Exit",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (reply == JOptionPane.YES_OPTION) {
            // Alert server of logout
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

            // Stop dispatcher
            dispatcher.stop();
            dispose();
            System.exit(0);
        }
    }

    /**
     * Add chat to the chatText message text pane.
     */
    public void addChat(String message) {
        try {
            chatText.insertString(chatText.getLength(), message + "\n", null);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    /**
     * Add event to the eventText text pane.
     */
    public void addEvent(String message) {
        try {
            eventText.insertString(eventText.getLength(), message + "\n", null);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    /**
     * Add image to the event panel.
     */
    public void addImage(ImageIcon image) {
        event.insertIcon(image);
    }

    /**
     * Updates the screen showing new logged in users.
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

    private class YellowChangeListener implements ChangeListener {
        JButton rolledBtn;
        @Override
        public void stateChanged(ChangeEvent e) {
            rolledBtn = (JButton)e.getSource();
            if (rolledBtn.getModel().isRollover())
                rolledBtn.setBackground(yellow);
            else
                rolledBtn.setBackground(beige);
        }
    }

    private class OrangeChangeListener implements ChangeListener {
        JButton rolledBtn;
        @Override
        public void stateChanged(ChangeEvent e) {
            rolledBtn = (JButton)e.getSource();
            if (rolledBtn.getModel().isRollover())
                rolledBtn.setBackground(yellow);
            else
                rolledBtn.setBackground(orange);
        }
    }
}
