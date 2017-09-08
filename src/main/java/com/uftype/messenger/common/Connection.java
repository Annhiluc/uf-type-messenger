package com.uftype.messenger.common;

import com.google.protobuf.InvalidProtocolBufferException;
import com.sun.org.apache.regexp.internal.RE;
import com.uftype.messenger.proto.ChatMessage;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;

public class Connection {
    public static HashMap<String, Thread> sendThreads;
    public static HashMap<String, Thread> receiveThreads;

    public Connection() {
        sendThreads = new HashMap<String, Thread>();
        receiveThreads = new HashMap<String, Thread>();
    }

    public static void connect(Socket sock) {
        startListening(sock);
    }

    public static void connect (String host, int port) {
        startListening(host, port);
    }

    private static class SendReceiveThread extends Thread {
        public static Socket socket; // Socket used for communication
        public static BufferedReader screenReader, receiveReader; // Reader for reading the text on the screen, other for reading the text from connection
        public static OutputStream outputStream; // Output stream of the socket for writing to connection
        public static InputStream inputStream; // Input stream of the socket for reading from connection

        public static boolean isReceiving; // True if the server/client is up

        public SendReceiveThread(Socket socket) {
            this.socket = socket;
            isReceiving = true;
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
    }

    private static class SendThread extends SendReceiveThread {
        public SendThread(Socket socket) {
            super(socket);
        }

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

    private static class ReceiveThread extends SendReceiveThread {
        public ReceiveThread(Socket socket) {
            super(socket);
        }

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

    private static void startListening(String host, int port) {
        try {
            Socket socket = new Socket(host, port);
            startListening(socket);
        } catch (IOException e) {
            System.out.println("Error while connecting to the messenger.");
        }
    }

    private static void startListening(Socket socket) {
        // Create two threads, one to listen for messages, one for listening to typing on screen
        SendReceiveThread sendMessageThread = new SendThread(socket);
        SendReceiveThread receiveMessageThread = new ReceiveThread(socket);

        sendThreads.put(socket.getInetAddress().toString(), sendMessageThread);
        receiveThreads.put(socket.getInetAddress().toString(), receiveMessageThread);

        sendMessageThread.start();
        receiveMessageThread.start();
    }

    public static void disconnect() {
        // Disconnect every client in the hashmap
    }
}
