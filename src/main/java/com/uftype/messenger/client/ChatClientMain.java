package com.uftype.messenger.client;

import java.io.IOException;
import java.util.Scanner;

public class ChatClientMain {
    public static void main(String[] args) throws IOException {
        try {
            Scanner in = new Scanner(System.in);

            System.out.println("Please enter the host you want to chat with: ");
            String host = in.nextLine();
            System.out.println("Please enter the port you want to communicate on: ");
            int port = in.nextInt();

            ChatClient client = new ChatClient(host, port);
        } catch (IOException e) {
        }
    }
}
