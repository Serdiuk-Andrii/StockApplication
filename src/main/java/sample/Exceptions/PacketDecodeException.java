/**This file defines an exception which may occur while converting a packet, for instance,
 * an invalid Utilities.CRC16 control sum.
 * File: PacketReceiveException.java
 * Author: Serdiuk Andrii
 **/

package Exceptions;

public class PacketDecodeException extends Exception{

    public PacketDecodeException(String error) {
        super(error);
    }

}
