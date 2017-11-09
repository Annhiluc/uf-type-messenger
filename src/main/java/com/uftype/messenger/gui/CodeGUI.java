package com.uftype.messenger.gui;

import org.fife.ui.rsyntaxtextarea.FileLocation;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.TextEditorPane;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

public class CodeGUI extends JFrame {
    // Map between languages and new syntax pane
    private static final HashMap<String, String> languageMap = new HashMap<>();
    private static final HashMap<String, String> fileExtensionMap = new HashMap<>();

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

        fileExtensionMap.put("C", "c");
        fileExtensionMap.put("C++", "cpp");
        fileExtensionMap.put("C#", "cs");
        fileExtensionMap.put("Java", "java");
        fileExtensionMap.put("JSON", "json");
        fileExtensionMap.put("JavaScript", "js");
        fileExtensionMap.put("Python", "py");
        fileExtensionMap.put("CSS", "css");
        fileExtensionMap.put("HTML", "html");
    }

    public CodeGUI(String sender, String code, final String language) {
        super("Code from " + sender + " in " + language);
        JPanel cp = new JPanel(new GridLayout(1, 1));

        // Set the editing style depending on the type of language
        TextEditorPane textArea = new TextEditorPane(TextEditorPane.INSERT_MODE);
        textArea.setFont(textArea.getFont().deriveFont(24.0f));
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

        // Size the frame.
        setSize(1000, 800);

        setVisible(true);

        try {
            textArea.setEncoding("UTF8");
            textArea.setName("Code_Snippet" + fileExtensionMap.get(language));
            textArea.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
