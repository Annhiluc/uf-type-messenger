package com.uftype.messenger.gui;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

public class CodeGUI extends JFrame {
    public RSyntaxTextArea textArea;
    protected static final HashMap<String, String> languageMap = new HashMap<String, String>();
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
        JPanel cp = new JPanel(new GridLayout(1,1));

        textArea = new RSyntaxTextArea(20, 60);
        textArea.setSyntaxEditingStyle("text/" + languageMap.get(language));
        textArea.setCodeFoldingEnabled(true);
        textArea.setText(code);
        RTextScrollPane sp = new RTextScrollPane(textArea);
        cp.add(sp);

        setContentPane(cp);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);

        setVisible(true);
    }
}
