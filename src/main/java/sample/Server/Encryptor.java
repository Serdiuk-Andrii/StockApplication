/*This file contains a class that encrypts the message and tells the corresponding sender class to
  send this message on a network.
  File: Encryptor.java
  Author: Serdiuk Andrii
  */


package Server;

import sample.Interfaces.ConsciousRunnable;
import Server.Packet;
import java.util.concurrent.BlockingQueue;

public class Encryptor implements ConsciousRunnable {

    private final BlockingQueue<Packet> responsesToEncrypt;
    private final BlockingQueue<Packet> responsesToSend;
    private boolean isAlive;

    public Encryptor(BlockingQueue<Packet> responsesToEncrypt, BlockingQueue<Packet> responsesToSend) {
        this.responsesToEncrypt = responsesToEncrypt;
        this.responsesToSend = responsesToSend;
    }

    public boolean isAlive() {
        return isAlive;
    }


    @Override
    public void run() {
        isAlive = true;
        try {
            while(true) {
                Packet packetToSend = responsesToEncrypt.take();
                if (packetToSend.getMessage() == null) {
                    responsesToSend.add(packetToSend);
                    break;
                }
                responsesToSend.add(packetToSend);
            }
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }  finally {
            isAlive = false;
        }
    }
}
