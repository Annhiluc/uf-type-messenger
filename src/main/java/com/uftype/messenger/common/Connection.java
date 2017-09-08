package com.uftype.messenger.common;

import com.uftype.messenger.proto.ChatMessage;

import java.io.*;
import java.net.Socket;

public class Connection extends Thread {
    public static Socket socket; // Socket used for communication
    public static BufferedReader screenReader, receiveReader; // Reader for reading the text on the screen, other for reading the text from connection
    public static OutputStream outputStream; // Output stream of the socket for writing to connection
    public static InputStream inputStream; // Input stream of the socket for reading from connection

    public static boolean isReceiving; // True if the server/client is up

    public static Thread sendThread;
    public static Thread receiveThread;

    public Connection(Socket socket) {
        this.socket = socket;
        isReceiving = true;
    }

    public void run() {
        initialize();
        startListening(socket);
    }

    public static void initialize() {
        try {
            screenReader = new BufferedReader(new InputStreamReader(System.in));
            outputStream = socket.getOutputStream();
            inputStream = socket.getInputStream();
            receiveReader = new BufferedReader(new InputStreamReader(inputStream));
        } catch (IOException e) {
            System.out.println("Error while initializing input and output streams: " + e);
        }
    }

    private static void startListening(Socket socket) {
        // Create two threads, one to listen for messages, one for listening to typing on screen
        sendThread = new SendThread();
        receiveThread = new ReceiveThread();

        sendThread.start();
        receiveThread.start();
    }

    public static void disconnect() {
        isReceiving = false;
    }

    private static class SendThread extends Thread {
        public void run() {
            try {
                String sendMsg;
                ChatMessage.Message.Builder message = ChatMessage.Message.newBuilder();

                while (isReceiving) {
                    // Create two threads, one to listen for messages, one for listening to typing on screen
                    sendMsg = screenReader.readLine();

                    // Build message here
                    message.setSender(socket.getLocalSocketAddress().toString());
                    message.setRecipient(socket.getRemoteSocketAddress().toString());
                    message.setText(socket.getLocalSocketAddress().toString() + ": " + sendMsg);
                    message.build().writeDelimitedTo(outputStream);
                    outputStream.flush();
                }
            } catch (Exception e) {
                if (e.getMessage().equals("Connection reset")) {
                    System.out.println("Connection closed with the remote host.");
                }
                else {
                    System.out.println("Error while receiving message from UF TYPE messenger: " + e);
                }
            }
        }
    }

    private static class ReceiveThread extends Thread {
        public void run() {
            try {
                String receiveMsg;
                while (isReceiving) {
                    // Create two threads, one to listen for messages, one for listening to typing on screen
                    ChatMessage.Message message = ChatMessage.Message.parseDelimitedFrom(inputStream);
                    System.out.println(message.getText());
                }
            } catch (Exception e) {
                if (e.getMessage().equals("Connection reset")) {
                    System.out.println("Connection closed with the remote host.");
                }
                else {
                    System.out.println("Error while receiving message from UF TYPE messenger: " + e);
                }
            }
        }
    }
}
