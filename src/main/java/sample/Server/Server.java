
package Server;

import sample.Interfaces.Connection;
import Server.Packet;
import Server.Decryptor;
import Server.Encryptor;
import Server.Processor;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;


public abstract class Server {

    public static final int PROCESSORS_PARTS = 3;
    protected int connectionsAmount, processorsAmount;
    protected BlockingQueue<byte[]> packetsToDecode;
    protected BlockingQueue<Packet> responsesToSend, messagesToProcess, responsesToEncrypt;
    protected Connection[] connections;
    protected Thread[] processors;


    public Server(int connectionsAmount, int processorsAmount) {

        this.connectionsAmount = connectionsAmount;
        this.processorsAmount = processorsAmount;
        this.packetsToDecode = new LinkedBlockingDeque<>();
        this.responsesToSend = new LinkedBlockingDeque<>();
        this.messagesToProcess = new LinkedBlockingDeque<>();
        this.responsesToEncrypt = new LinkedBlockingDeque<>();
        connections = new Connection[connectionsAmount];
        processors = new Thread[PROCESSORS_PARTS*processorsAmount];
    }


    public void start() {
        int j = 0;
        for (int i = 0; i < processorsAmount; i++) {
            (processors[j++] = new Thread(new Decryptor(packetsToDecode, messagesToProcess))).start();
            (processors[j++] = new Thread(new Processor(messagesToProcess, responsesToEncrypt))).start();
            (processors[j++] = new Thread(new Encryptor(responsesToEncrypt, responsesToSend))).start();
        }
        startConnections();
    }

    protected void stopProcessors() throws InterruptedException {
        //Adding sleeping pills to the queues
        for (int i = 0; i < processorsAmount; i++)
            packetsToDecode.add(new byte[0]);
        for (int i = 0; i < processorsAmount*PROCESSORS_PARTS; i++)
            processors[i].join();

    }

    public void stop() {
        try {
            stopProcessors();
            stopConnections();
        } catch (InterruptedException e) {
            System.err.println("An error occurred while closing the server");
        }
    }

    public boolean hasStopped() {
        for (int i = 0; i < connectionsAmount; i++)
            if (connections[i].isAlive())
                return false;
        for (int i = 0; i < processorsAmount*Server.PROCESSORS_PARTS; i++)
            if (processors[i].isAlive())
                return false;
        return true;
    }


    protected void stopConnections() {
        for (int i = 0; i < connectionsAmount; i++)
            connections[i].stopConnection();
    }

    protected abstract void startConnections();



}
