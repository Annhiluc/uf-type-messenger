package com.uftype.messenger.gui;

import com.uftype.messenger.auth.Authentication;
import com.uftype.messenger.common.UserContext;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Login extends JFrame implements ActionListener {
    protected JTextField username;
    protected JPasswordField password;
    protected JPanel loginPanel;
    protected JButton submit;
    public UserContext loggedInUser;
    protected GUI gui;

    public Login (GUI gui) {
        super("Login");

        loggedInUser = null;
        this.gui = gui;

        loginPanel = new JPanel(new GridLayout(3,1));
        JLabel userLabel = new JLabel("Username:");
        username = new JTextField(1);
        JLabel passLabel = new JLabel("Password:");
        password = new JPasswordField(1);
        submit = new JButton("Submit");

        submit.addActionListener(this);

        loginPanel.add(userLabel);
        loginPanel.add(username);
        loginPanel.add(passLabel);
        loginPanel.add(password);
        loginPanel.add(submit);
        add(loginPanel, BorderLayout.CENTER);

        // Add the file menu
        JMenuBar menuBar = new JMenuBar();
        JMenu menuFile = new JMenu("File");
        JMenuItem menuItemExit = new JMenuItem("Exit");
        menuFile.add(menuItemExit);
        menuBar.add(menuFile);
        // adds menu bar to the frame
        setJMenuBar(menuBar);

        // Size the frame.
        //frame.pack();
        setSize(600,400);

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
}
