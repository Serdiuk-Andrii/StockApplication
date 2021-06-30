/*This file defines an interface of a common sender in a network
  File: Interfaces.Sender.java
  Author: Serdiuk Andrii
  */

package sample.Interfaces;

import java.io.IOException;
import java.net.InetAddress;

public interface Sender {


    void sendMessage(byte[] message, InetAddress target, int port) throws IOException;


}
