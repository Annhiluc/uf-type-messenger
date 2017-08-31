package com.uftype.messenger.server;

import java.io.IOException;

public class ChatServerMain {
    public static void main(String[] args) throws IOException{
        try {
            ChatServer server = new ChatServer(3000);
        }
        catch (IOException e) {}
    }
}
