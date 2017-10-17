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
    protected JTextField usernameLogin, usernameRegister, firstName, lastName, email;
    protected JPasswordField passwordLogin, passwordRegister;
    protected JPanel title, loginPanel, submitPanel, registerPanel;
    protected JButton submit, loginRegister;
    public UserContext loggedInUser;
    protected GUI gui;

    public Login (GUI gui) {
        super("UF TYPE Messenger");

        loggedInUser = null;
        this.gui = gui;

        loginRegister = new JButton("Register");
        loginRegister.addActionListener(this);
        loginRegister.setEnabled(true);

        title = new JPanel(new GridLayout(3,1));
        ImageIcon ii = new ImageIcon("src/main/resources/type-logo.png");
        JLabel label = new JLabel(ii);
        JLabel welcome = new JLabel("Welcome to the UF TYPE Messenger!");
        welcome.setHorizontalAlignment(0);
        title.add(label);
        title.add(welcome);
        title.add(loginRegister);
        add(title, BorderLayout.NORTH);

        loginPanel = new JPanel(new GridLayout(2,1));
        JLabel userLabel = new JLabel("Username:");
        usernameLogin = new JTextField(1);
        JLabel passLabel = new JLabel("Password:");
        passwordLogin = new JPasswordField(1);

        loginPanel.add(userLabel);
        loginPanel.add(usernameLogin);
        loginPanel.add(passLabel);
        loginPanel.add(passwordLogin);
        add(loginPanel, BorderLayout.CENTER);

        registerPanel = new JPanel(new GridLayout(5, 1));
        JLabel firstLabel = new JLabel("First name:");
        firstName = new JTextField(1);
        JLabel lastLabel = new JLabel("Last name:");
        lastName = new JTextField(1);
        userLabel = new JLabel("Username:");
        usernameRegister = new JTextField(1);
        passLabel = new JLabel("Password:");
        passwordRegister = new JPasswordField(1);
        // Consider adding another password field here to verify password
        JLabel emailLabel = new JLabel("Email:");
        email = new JTextField(1);
        // Consider adding another email field here to verify email

        registerPanel.add(firstLabel);
        registerPanel.add(firstName);
        registerPanel.add(lastLabel);
        registerPanel.add(lastName);
        registerPanel.add(userLabel);
        registerPanel.add(usernameRegister);
        registerPanel.add(passLabel);
        registerPanel.add(passwordRegister);
        registerPanel.add(emailLabel);
        registerPanel.add(email);

        // Will add this panel to frame when register button pressed

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

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE); // Will not close unless prompted
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();

        if (o == submit) {
            if (loginRegister.getText().equals("Register")) {
                // On login page
                String user = usernameLogin.getText().trim();
                String pass = new String(passwordLogin.getPassword()).trim();

                if (Authentication.authenticate(user, pass) != null) {
                    loggedInUser = Authentication.login(user, pass);
                    gui.loadScreen();
                    // This window closes.
                    dispose();
                }
                else {
                    JOptionPane.showMessageDialog(Login.this, "Incorrect credentials. Please try again.");
                    usernameLogin.setText("");
                    passwordLogin.setText("");
                    return;
                }
            }
            else {
                // On register page
                String user = usernameRegister.getText().trim();
                String pass = new String(passwordRegister.getPassword()).trim();
                String em = email.getText().trim();
                String first = firstName.getText().trim();
                String last = lastName.getText().trim();

                if (Authentication.register(first, last, user, em, pass)) {
                    loggedInUser = Authentication.login(user, pass);
                    gui.loadScreen();
                    // This window closes.
                    dispose();
                }
                else {
                    JOptionPane.showMessageDialog(Login.this, "Invalid credentials. Please try again.");
                    usernameRegister.setText("");
                    passwordRegister.setText("");
                    email.setText("");
                    firstName.setText("");
                    lastName.setText("");
                    return;
                }
            }
        }
        else if (o == loginRegister) {
            if (loginRegister.getText().equals("Register")) {
                // Switch to register page with fields for name, username, etc.
                remove(loginPanel);
                add(registerPanel, BorderLayout.CENTER);
                loginRegister.setText("Login");
                repaint(); // Automatically update the screen

            } else {
                // Switch to login page
                remove(registerPanel);
                add(loginPanel, BorderLayout.CENTER);
                loginRegister.setText("Register");
                validate(); // Automatically update the screen
                repaint();
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
