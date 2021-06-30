/*This class defines a TCP connection on the side of the server.
 * File: TCPConnection.java
 * Author: Serdiuk Andrii
 * */

package Server;

import sample.Interfaces.Connection;
import Server.Packet;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;

public class TCPConnection implements Connection {


    private final BlockingQueue<byte[]> packetsToDecode;
    private final BlockingQueue<Packet> responsesToSend;
    private InputStream inputStream;
    private OutputStream outputStream;
    private final ServerSocket serverSocket;
    private boolean isAlive = true;
    private short messagesReceived;
    private short messagesSent;

    @Override
    public void stopConnection() {
        isAlive = false;
    }

    public TCPConnection(ServerSocket serverSocket, BlockingQueue<byte[]> packetsToDecode, BlockingQueue<Packet> responsesToSend) throws IOException {
        this.packetsToDecode = packetsToDecode;
        this.responsesToSend = responsesToSend;
        this.serverSocket = serverSocket;

    }

    @Override
    public void run() {
        try {
            Socket socket = serverSocket.accept();
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
        } catch (SocketException e) {
            isAlive = false;
            return;
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            while(isAlive) {
                receiveMessage();
                sendMessage(responsesToSend.take().convertToPacket(), null, -1);
            }
        } catch (Exception e) {
            System.out.println("Connection has been aborted");
        } finally {
            System.out.println("TCPConnection has been stopped");
        }
    }

    @Override
    public void receiveMessage() throws Exception {
        byte[] buffer = new byte[Packet.MAX_SIZE];
        inputStream.read(buffer);
        System.out.println("Received message: " + (new Packet(buffer).getMessage().getMessageText()));
        packetsToDecode.add(buffer);
        messagesReceived++;
    }

    @Override
    public void sendMessage(byte[] message, InetAddress ignoredAddress, int ignoredPort) throws IOException {
        outputStream.write(message);
        messagesSent++;
    }

    @Override
    public boolean isAlive() {
        return isAlive;
    }

    public short getMessagesReceived() {
        return messagesReceived;
    }

    public short getMessagesSent() {
        return messagesSent;
    }
}
