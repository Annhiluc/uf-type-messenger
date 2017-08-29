package com.uftype.messenger.client;

import java.io.*;
import java.net.Socket;

/* Represents the chat client. */
public class ChatClient {
    private Socket socket; // Socket used for communication with the server
    private BufferedReader screenReader, receiveReader; // Reader for reading the text on the screen, other for reading the text from the server
    private PrintWriter writer; // Writer to the server
    private OutputStream outputStream; // Output stream of the socket for writing to server
    private InputStream inputStream; // Input stream of the socket for reading from server

    private boolean isReceiving; // True if currently receiving messages

    public ChatClient(String host, int port) throws IOException {
        connect(host, port);
    }

    /* Connect to the host with port number and start communicating to server.
       Read in text from the server as well as communicate to the server.
     */
    private void connect(String host, int port) throws IOException {
        try {
            socket = new Socket(host, port);
            screenReader = new BufferedReader(new InputStreamReader(System.in));
            outputStream = socket.getOutputStream();
            writer = new PrintWriter(outputStream, true);

            inputStream = socket.getInputStream();
            receiveReader = new BufferedReader(new InputStreamReader(inputStream));

            isReceiving = true;

            // Can now speak with server (chat room)
            System.out.println("You've connected to the chat room. Chat away!");

            String receiveMsg, sendMsg;
            while(isReceiving) {
                sendMsg = screenReader.readLine();
                writer.println(sendMsg);
                writer.flush();

                if ((receiveMsg = receiveReader.readLine()) != null) {
                    System.out.println(receiveMsg);
                }
            }
        }
        catch (IOException e) {
            System.out.println("Uh-oh! Something happened: " + e);
        }
    }

    private void disConnect() {
        isReceiving = false;
    }
}
