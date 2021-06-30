/*
* File: StoreServerTCP.java
* Author: Serdiuk Andrii
* */


package Server;


import Server.Database.Database;
import Server.Server;
import Server.TCPConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;

public class StoreServerTCP extends Server{

    private final ServerSocket serverSocket;

    public StoreServerTCP(int connectionsAmount, int processorsAmount, int port) throws IOException {
        super(connectionsAmount, processorsAmount);
        serverSocket = new ServerSocket(port);
        System.out.println("Server has been started");
        Database.getConnection();
        start();
    }

    @Override
    protected void startConnections() {
        try {
            for (int i = 0; i < connectionsAmount; i++)
                new Thread(connections[i] = new TCPConnection(serverSocket, packetsToDecode, responsesToSend)).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void stop() {
        super.stop();
        try {
            serverSocket.close();
        } catch (IOException e) {
            System.err.println("Socket has not been closed");
        }
        Database.close();
    }


    public static void main(String[] args) throws IOException {
        StoreServerTCP server = new StoreServerTCP(16, 2, Integer.parseInt(args[0]));
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
    }

}
