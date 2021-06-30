/*This file defines an interface that unites Receiver and Sender interfaces
 * File: Connection.java
 * Author: Serdiuk Andrii
 * */

package sample.Interfaces;

public interface Connection extends Receiver, Sender, ConsciousRunnable{

    void stopConnection();

}
