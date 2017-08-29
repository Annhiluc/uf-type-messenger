package com.uftypemessenger;

import java.io.IOException;

public class ChatEntryPoint {

    public static void main(String[] args) {
        // Using for starting point: https://way2java.com/networking/chat-program-two-way-communication/

        // Create two threads simulating a chat client and the server
        Thread t1 = new Thread(new ChatClientRunnable());
        t1.start();

        Thread t2 = new Thread(new ChatServerRunnable());
        t2.start();
    }
}

class ChatClientRunnable implements Runnable {
    public void run() {
        try {
            ChatClient client = new ChatClient("127.0.0.1", 3000);
        }
        catch (IOException e) {}
    }
}

class ChatServerRunnable implements Runnable {
    public void run() {
        try {
            ChatServer server = new ChatServer(3000);
        }
        catch (IOException e) {}
    }
}

