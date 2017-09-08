package com.uftype.messenger.client;

import java.io.IOException;
import java.util.Scanner;

public class ClientMain {
    public static void main(String[] args) throws IOException {
        try {
            Scanner in = new Scanner(System.in);

            System.out.println("Please enter the host you want to chat with: ");
            String host = in.nextLine();
            System.out.println("Please enter the port you want to communicate on: ");
            int port = in.nextInt();

            Client client = new Client(host, port);
        } catch (IOException e) {
        }
    }
}
