package com.uftype.messenger.server;

import java.io.IOException;

public class ServerMain {
    public static void main(String[] args) throws IOException {
        try {
            Server server = new Server(3000);
        } catch (IOException e) {
        }
    }
}
