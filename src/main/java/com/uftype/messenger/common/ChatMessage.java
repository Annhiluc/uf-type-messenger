package com.uftype.messenger.common;

import java.security.Timestamp;

public class ChatMessage {
    enum Type {
        LOGIN,
        LOGOUT,
        NEWUSER,
        TEXT
    }

    String content;
    String sender;
    String recipient;
    Timestamp timestamp;
}
