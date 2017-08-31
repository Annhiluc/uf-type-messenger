package com.uftype.messenger.client;

import java.io.IOException;

public class ChatClientMain {
    public static void main(String[] args) throws IOException{
        try {
            ChatClient client = new ChatClient("127.0.0.1", 3000);
        }
        catch (IOException e) {}
    }
}
