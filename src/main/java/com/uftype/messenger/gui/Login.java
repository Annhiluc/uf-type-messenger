package com.uftype.messenger.gui;

import com.uftype.messenger.auth.Authentication;
import com.uftype.messenger.common.UserContext;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class Login extends JFrame implements ActionListener, WindowListener {
    protected JTextField username;
    protected JPasswordField password;
    protected JPanel title, loginPanel, submitPanel;
    protected JButton submit;
    public UserContext loggedInUser;
    protected GUI gui;

    public Login (GUI gui) {
        super("Login");

        loggedInUser = null;
        this.gui = gui;

        title = new JPanel(new GridLayout(2,1));
        ImageIcon ii = new ImageIcon("src/main/resources/type-logo.png");
        JLabel label = new JLabel(ii);
        JLabel welcome = new JLabel("Welcome to the UF TYPE Messenger!");
        welcome.setHorizontalAlignment(0);
        title.add(label);
        title.add(welcome);
        add(title, BorderLayout.NORTH);

        loginPanel = new JPanel(new GridLayout(2,1));
        JLabel userLabel = new JLabel("Username:");
        username = new JTextField(1);
        JLabel passLabel = new JLabel("Password:");
        password = new JPasswordField(1);

        loginPanel.add(userLabel);
        loginPanel.add(username);
        loginPanel.add(passLabel);
        loginPanel.add(password);
        add(loginPanel, BorderLayout.CENTER);

        submitPanel = new JPanel(new GridLayout(1,1));
        submit = new JButton("Submit");
        submit.addActionListener(this);
        submitPanel.add(submit);
        add(submitPanel, BorderLayout.SOUTH);

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
        setSize(1200,800);

        // Set the frame icon to an image loaded from a file.
        setIconImage(new ImageIcon("src/main/resources/type-icon.png").getImage());

        // Show it.
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();

        if (o == submit) {
            // Handle other actions
            String user = username.getText().trim();
            String pass = new String(password.getPassword()).trim();

            if (Authentication.authenticate(user, pass) != null) {
                loggedInUser = Authentication.login(user, pass);
                gui.loadScreen();
                // This window closes.
                dispose();
            }
            else {
                JOptionPane.showMessageDialog(Login.this, "Incorrect credentials. Please try again.");
                username.setText("");
                password.setText("");
                return;
            }
        }
    }

    @Override
    public void windowClosing(WindowEvent e) {
        int reply = JOptionPane.showConfirmDialog(Login.this,
                "Are you sure you want to quit?",
                "Exit",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (reply == JOptionPane.YES_OPTION) {
            // Stop program
            dispose();
            System.exit(0);
        } else {
            return;
        }
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
