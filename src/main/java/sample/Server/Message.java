/*This file defines a message on a network that is formed by some protocol:
 * File: Message.java
 * Author: Serdiuk Andrii
 **/

package Server;

import Exceptions.CryptoException;
import Server.Encrypt;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class Message implements Cloneable{

    private static Encrypt encrypt;

    static {
        try {
            encrypt = new Encrypt();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final int cType;
    private final int bUserId;
    private byte[] messageText;

    public static final int MESSAGE_ADDITIONAL_INFORMATION = 2*Integer.BYTES;
    public static final int MAX_SIZE = MESSAGE_ADDITIONAL_INFORMATION + 100000;


    public Message(int cType, int bUserId, byte[] messageText) {
        this.cType = cType;
        this.bUserId = bUserId;
        this.messageText = messageText;
    }

    public byte[] toBytePacket() {
        return ByteBuffer.allocate(getMessageLength()).putInt(cType).putInt(bUserId).put(messageText).array();
    }

    public int getMessageLength() {
        return getMessageTextLength() + MESSAGE_ADDITIONAL_INFORMATION;
    }

    public int getMessageTextLength() {
        return messageText.length;
    }

    public Message encode() throws CryptoException {
        try {
            messageText = encrypt.encode(messageText);
        } catch (Exception e) {
            throw new CryptoException("Encode exception: " + e.getMessage());
        }
        return this;
    }

    public Message decode() throws CryptoException {
        try {
            messageText = encrypt.decode(messageText);
        } catch (Exception e) {
            throw new CryptoException("Decode exception: " + e.getMessage());
        }
        return this;
    }
    public String getMessageText() {
        return new String(messageText, StandardCharsets.UTF_8);
    }

    @Override
    public Message clone() throws CloneNotSupportedException {
        return (Message) super.clone();
    }

    /**@return The command type of the message
     **/
    public int getCType() {
        return cType;
    }

    /**
     * @return The id of the user
     */
    public int getBUserId() {
        return bUserId;
    }

}
