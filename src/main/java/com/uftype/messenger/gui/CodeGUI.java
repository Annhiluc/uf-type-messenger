package com.uftype.messenger.gui;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

public class CodeGUI extends JFrame {
    // Map between languages and new syntax pane
    private static final HashMap<String, String> languageMap = new HashMap<>();

    static {
        languageMap.put("C", "c");
        languageMap.put("C++", "cpp");
        languageMap.put("C#", "cs");
        languageMap.put("Java", "java");
        languageMap.put("JSON", "json");
        languageMap.put("JavaScript", "javascript");
        languageMap.put("Python", "python");
        languageMap.put("CSS", "css");
        languageMap.put("HTML", "html");
    }

    public CodeGUI(String sender, String code, String language) {
        super("Code from " + sender + " in " + language);
        JPanel cp = new JPanel(new GridLayout(1, 1));

        // Set the editing style depending on the type of language
        RSyntaxTextArea textArea = new RSyntaxTextArea(20, 60);
        textArea.setSyntaxEditingStyle("text/" + languageMap.get(language));
        textArea.setCodeFoldingEnabled(true);
        textArea.setText(code);
        RTextScrollPane sp = new RTextScrollPane(textArea);
        cp.add(sp);

        // Set content and set visible
        setContentPane(cp);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);

        setVisible(true);
    }
}
