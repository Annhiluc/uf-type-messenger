package com.uftype.messenger.common;

import com.google.protobuf.InvalidProtocolBufferException;
import com.uftype.messenger.proto.ChatMessage;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class ChatConnection {
    public static Socket socket; // Socket used for communication
    public static BufferedReader screenReader, receiveReader; // Reader for reading the text on the screen, other for reading the text from connection
    public static OutputStream outputStream; // Output stream of the socket for writing to connection
    public static InputStream inputStream; // Input stream of the socket for reading from connection

    public static boolean isReceiving;

    public ChatConnection() {
        isReceiving = true;
    }

    public static void connect(ServerSocket server) {
        try {
            socket = server.accept();
            initialize();
            startListening();
        } catch (IOException e) {
            System.out.println("Error while connecting to server socket: " + e);
        }
    }

    public static void connect (String host, int port) {
        try {
            socket = new Socket(host, port);
            initialize();
            startListening();
        } catch (IOException e) {
            System.out.println("Error while connecting to socket: " + e);
        }
    }

    private static void initialize() {
        try {
            screenReader = new BufferedReader(new InputStreamReader(System.in));
            outputStream = socket.getOutputStream();
            inputStream = socket.getInputStream();
            receiveReader = new BufferedReader(new InputStreamReader(inputStream));
        } catch (IOException e) {
            System.out.println("Error while initializing input and output streams: " + e);
        }
    }

    private static class SendRunnable implements Runnable {
        public void run() {
            try {
                String sendMsg;
                ChatMessage.Message.Builder message = ChatMessage.Message.newBuilder();

                while (isReceiving) {
                    // Create two threads, one to listen for messages, one for listening to typing on screen
                    sendMsg = screenReader.readLine();

                    // Build message here
                    message.setText(sendMsg);
                    message.build().writeDelimitedTo(outputStream);
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

    private static class ReceiveRunnable implements Runnable {
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

    private static void startListening() {
        // Create two threads, one to listen for messages, one for listening to typing on screen
        Thread sendMessageThread = new Thread(new SendRunnable());
        Thread receiveMessageThread = new Thread(new ReceiveRunnable());

        sendMessageThread.start();
        receiveMessageThread.start();
    }

    public static void disconnect() {
        isReceiving = false;
    }
}
