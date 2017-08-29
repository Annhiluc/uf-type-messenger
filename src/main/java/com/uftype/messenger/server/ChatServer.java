package com.uftype.messenger.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class ChatServer {
    private ServerSocket serverSocket; // Socket for the server
    private Socket socket; // Socket used for communication with the server
    private BufferedReader screenReader, receiveReader; // Reader for reading the text on the screen, other for reading the text from the client
    private PrintWriter writer; // Writer to the client
    private OutputStream outputStream; // Output stream of the socket for writing to client
    private InputStream inputStream; // Input stream of the socket for reading from client

    private boolean isReceiving;

    private HashMap<String, PrintWriter> clients; // Mapping of client names to PrintWriters

    public ChatServer(int port) throws IOException {
        connect(port);
        clients = new HashMap<String, PrintWriter>();
    }

    /* Accept communications from the port from multiple clients. Push all information onto the screen.
       Send information to clients.
     */
    private  void connect(int port) throws IOException {
        try {
            serverSocket = new ServerSocket(port);
            socket = serverSocket.accept();

            // People can now post in the server (chat room)
            System.out.println("The chat room is ready for chatting.");

            screenReader = new BufferedReader(new InputStreamReader(System.in));
            outputStream = socket.getOutputStream();
            writer = new PrintWriter(outputStream, true);

            inputStream = socket.getInputStream();
            receiveReader = new BufferedReader(new InputStreamReader(inputStream));

            isReceiving = true;

            String receiveMsg, sendMsg;
            while(isReceiving) {
                if ((receiveMsg = receiveReader.readLine()) != null) {
                    System.out.println(receiveMsg);
                }

                sendMsg = screenReader.readLine();
                writer.println(sendMsg);
                writer.flush();
            }
        }
        catch (IOException e) {
            System.out.println("Uh-oh! Something happened: " + e);
        }
    }

    private  void disConnect() {
        isReceiving = false;
    }
}
