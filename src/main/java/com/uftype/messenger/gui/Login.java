package com.uftype.messenger.gui;

import com.uftype.messenger.auth.Authentication;
import com.uftype.messenger.common.UserContext;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.net.URL;

public class Login extends JFrame implements ActionListener, WindowListener {
    private JTextField usernameLogin, usernameRegister, firstName, lastName, email;     // Text field for registration
    private JPasswordField passwordLogin, passwordRegister;              // Password fields for login and registration
    private JPanel loginPanel, registerPanel;                            // Panel for login and for registration
    private JButton submit, loginRegister;                               // Button to submit the login information
    private Font monoFont = new Font(Font.DIALOG_INPUT, Font.BOLD, 24);              // Special font

    UserContext loggedInUser;       // Logged in user context

    protected GUI gui;              // Gui screen

    OrangeChangeListener orangeBtn = new OrangeChangeListener();
    YellowChangeListener yellowBtn = new YellowChangeListener();
    Color blue = new Color(13, 59, 102);
    Color orange = new Color(228, 150, 75);
    Color yellow = new Color(244, 211, 94);
    Color beige = new Color(250, 240, 202);

    Login(GUI gui) {
        super("UF TYPE Messenger");

        loggedInUser = null;
        this.gui = gui;

        // Add login and register button
        loginRegister = new JButton("Register");
        loginRegister.addActionListener(this);
        loginRegister.setEnabled(true);
        loginRegister.setFont(monoFont);
        loginRegister.setBackground(orange);
        loginRegister.addChangeListener(orangeBtn);

        // Add logo and title button
        JPanel title = new JPanel(new GridLayout(3, 1));
        //Get file from resources folder
        URL resource = getClass().getClassLoader().getResource("type-logo.png");
        if (resource != null) {
            ImageIcon ii = new ImageIcon(resource);
            JLabel label = new JLabel(ii);
            title.add(label);
        }
        JLabel welcome = new JLabel("Welcome to the UF TYPE Messenger!");
        welcome.setHorizontalAlignment(SwingConstants.CENTER);
        welcome.setFont(monoFont);
        welcome.setForeground(Color.white);
        title.add(welcome);
        title.add(loginRegister);
        title.setBackground(blue);
        add(title, BorderLayout.NORTH);

        // Login panel with username and password
        loginPanel = new JPanel(new GridLayout(2, 1));
        JLabel userLabel = new JLabel("Username:");
        userLabel.setFont(monoFont);
        userLabel.setForeground(Color.white);
        usernameLogin = new JTextField(1);
        usernameLogin.setFont(monoFont);
        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(monoFont);
        passLabel.setForeground(Color.white);
        passwordLogin = new JPasswordField(1);
        passwordLogin.setFont(monoFont);

        loginPanel.add(userLabel);
        loginPanel.add(usernameLogin);
        loginPanel.add(passLabel);
        loginPanel.add(passwordLogin);
        loginPanel.setBackground(blue);
        loginPanel.setForeground(Color.white);
        add(loginPanel, BorderLayout.CENTER);

        // Register panel with name, username, password, email
        registerPanel = new JPanel(new GridLayout(5, 1));
        JLabel firstLabel = new JLabel("First name:");
        firstLabel.setFont(monoFont);
        firstLabel.setForeground(Color.white);
        firstName = new JTextField(1);
        firstName.setFont(monoFont);
        JLabel lastLabel = new JLabel("Last name:");
        lastLabel.setFont(monoFont);
        lastLabel.setForeground(Color.white);
        lastName = new JTextField(1);
        lastName.setFont(monoFont);
        userLabel = new JLabel("Username:");
        userLabel.setFont(monoFont);
        userLabel.setForeground(Color.white);
        usernameRegister = new JTextField(1);
        usernameRegister.setFont(monoFont);
        passLabel = new JLabel("Password:");
        passLabel.setFont(monoFont);
        passLabel.setForeground(Color.white);
        passwordRegister = new JPasswordField(1);
        passwordRegister.setFont(monoFont);
        // Consider adding another password field here to verify password
        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(monoFont);
        emailLabel.setForeground(Color.white);
        email = new JTextField(1);
        email.setFont(monoFont);
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
        registerPanel.setBackground(blue);

        // Will add this panel to frame when register button pressed

        JPanel submitPanel = new JPanel(new GridLayout(1, 1));
        submit = new JButton("Submit");
        submit.addActionListener(this);
        submit.setFont(monoFont);
        submit.setBackground(orange);
        submit.addChangeListener(orangeBtn);
        submitPanel.add(submit);
        add(submitPanel, BorderLayout.SOUTH);

        addWindowListener(this);

        // Size the frame.
        setSize(1500, 1000);

        // Set the frame icon to an image loaded from a file.
        URL icon = getClass().getClassLoader().getResource("type-icon.png");

        if (icon != null) {
            setIconImage(new ImageIcon(icon).getImage());
        }
        // Show it.
        setVisible(true);

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE); // Will not close unless prompted
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();

        if (o == submit) {
            // Either login or register
            if (loginRegister.getText().equals("Register")) {
                // On login page
                String user = usernameLogin.getText().trim();
                String pass = new String(passwordLogin.getPassword()).trim();

                if (Authentication.authenticate(user, pass) != null) {
                    loggedInUser = Authentication.login(user, pass);
                    gui.loadScreen();
                    // This window closes.
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(Login.this, "Incorrect credentials. Please try again.");
                    usernameLogin.setText("");
                    passwordLogin.setText("");
                }
            } else {
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
                } else {
                    JOptionPane.showMessageDialog(Login.this, "Invalid credentials. Please try again.");
                    usernameRegister.setText("");
                    passwordRegister.setText("");
                    email.setText("");
                    firstName.setText("");
                    lastName.setText("");
                }
            }
        } else if (o == loginRegister) {
            // Login or register
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

    /**
     * Add label to verify exit.
     */
    @Override
    public void windowClosing(WindowEvent e) {
        // Create label to prompt when users try to close.
        JLabel label = new JLabel("Are you sure you want to quit?");
        label.setFont(monoFont);

        // Open confirm dialog with label
        int reply = JOptionPane.showConfirmDialog(Login.this,
                label,
                "Exit",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (reply == JOptionPane.YES_OPTION) {
            // Stop program
            dispose();
            System.exit(0);
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
