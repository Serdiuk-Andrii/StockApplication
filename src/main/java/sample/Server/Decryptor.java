/*This file contains a class that decrypts the message and passes it to the corresponding Classes.Processor class
  File: Decryptor.java
  Author: Serdiuk Andrii
  */


package Server;

import Exceptions.CryptoException;
import Exceptions.PacketDecodeException;
import com.google.common.primitives.UnsignedLong;
import sample.Interfaces.ConsciousRunnable;
import Server.Packet;

import java.util.concurrent.BlockingQueue;


public class Decryptor implements ConsciousRunnable {

    private final BlockingQueue<byte[]> packetsToDecode;
    private final BlockingQueue<Packet> messagesToProcess;
    private boolean isAlive;

    public boolean isAlive() {
        return isAlive;
    }

    public Decryptor(BlockingQueue<byte[]> packetsToDecode, BlockingQueue<Packet> messagessToProcess) {
        this.packetsToDecode = packetsToDecode;
        this.messagesToProcess = messagessToProcess;
    }


    @Override
    public void run() {
        isAlive = true;
        try {
            while(true) {
                byte[] input = packetsToDecode.take();
                if (input.length == 0) {
                    messagesToProcess.add(new Packet((byte) -1, UnsignedLong.MAX_VALUE, null));
                    break;
                }
                Packet packet = new Packet(input);
                messagesToProcess.add(packet);
            }
        } catch(InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (PacketDecodeException e) {
            e.printStackTrace();
        } catch (CryptoException e) {
            e.printStackTrace();
        } finally {
            isAlive = false;
        }
    }

}
