/*This file defines a packet on a network that is formed by the protocol defined in class Classes.Message
 * File: Packet.java
 * Author: Serdiuk Andrii
 **/

package Server;

import Exceptions.CryptoException;
import Exceptions.PacketDecodeException;
import Utilities.CRC16;
import Server.Message;
import com.google.common.primitives.UnsignedLong;

import java.nio.ByteBuffer;


public class Packet {

    public final static Byte MAGIC_BYTE = 0x13;
    public final static int PACKET_FIRST_PART_LENGTH = 2 * Byte.BYTES + Long.BYTES + Integer.BYTES;
    public final static int ADDITIONAL_INFORMATION_LENGTH = PACKET_FIRST_PART_LENGTH + 2 * Short.BYTES;
    public final static int MAX_SIZE = ADDITIONAL_INFORMATION_LENGTH + Message.MAX_SIZE;
    public final static int USER_ID_OFFSET = 2*Byte.BYTES + Short.BYTES + 2*Integer.BYTES + Long.BYTES;

    private Byte bSrc;
    private UnsignedLong bPktId;
    private Message message;

    private Short crc16First;
    private Short crc16Second;


    public Packet(Byte bSrc, UnsignedLong bPktId, Message message) {
        this.bSrc = bSrc;
        this.bPktId = bPktId;
        this.message = message;
    }

    public Packet(byte[] encodedPacket) throws PacketDecodeException, CryptoException {
        ByteBuffer buffer = ByteBuffer.wrap(encodedPacket);
        Byte expectedMagicByte = buffer.get();
        if (!expectedMagicByte.equals(MAGIC_BYTE))
            throw new PacketDecodeException("Unexpected magic byte");

        bSrc = buffer.get();
        bPktId = UnsignedLong.fromLongBits(buffer.getLong());
        int wLen = buffer.getInt();
        crc16First = buffer.getShort();
        if (!firstCrcSumIsCorrect(wLen))
            throw new PacketDecodeException("First crc16 sum does not match the expected one");

        int cType = buffer.getInt();
        int bUserId = buffer.getInt();
        byte[] messageText = new byte[wLen - 2 * Integer.BYTES];
        buffer.get(messageText);
        message = new Message(cType, bUserId, messageText);
        crc16Second = buffer.getShort();
        if (!secondCrcSumIsCorrect())
            throw new PacketDecodeException("Second crc16 sum does not match the expected one");
        message.decode();
    }

    private boolean firstCrcSumIsCorrect(int wLen) {
        short crc = (short) CRC16.crc16(ByteBuffer.allocate(PACKET_FIRST_PART_LENGTH).put(MAGIC_BYTE).put(bSrc)
                .putLong(bPktId.longValue()).putInt(wLen).array());
        return crc16First == crc;
    }

    private boolean secondCrcSumIsCorrect() {
        short crc = (short) CRC16.crc16(message.toBytePacket());
        return crc16Second == crc;
    }

    public byte[] convertToPacket() throws CryptoException {
        message.encode();

        byte[] packetFirstPart = ByteBuffer.allocate(PACKET_FIRST_PART_LENGTH).put(MAGIC_BYTE)
                .put(bSrc).putLong(bPktId.longValue()).putInt(message.getMessageLength()).array();
        crc16First = (short) CRC16.crc16(packetFirstPart);

        byte[] packetSecondPart = ByteBuffer.allocate(message.getMessageLength()).put(message.toBytePacket()).array();
        crc16Second = (short) CRC16.crc16(packetSecondPart);

        return ByteBuffer.allocate(message.getMessageLength() + ADDITIONAL_INFORMATION_LENGTH).put(packetFirstPart).putShort(crc16First)
                .put(packetSecondPart).putShort(crc16Second).array();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if(obj.getClass() != this.getClass()) return false;
        Packet that = (Packet) obj;
        return (that.getbPktId().equals(bPktId) && that.getbSrc().equals(bSrc)
                && that.getMessage().equals(message) && that.getCrc16First() == crc16First
                && that.getCrc16Second() == crc16Second);
    }

    public Byte getbSrc() {
        return bSrc;
    }

    public UnsignedLong getbPktId() {
        return bPktId;
    }

    public Message getMessage() {
        return message;
    }

    public Short getCrc16First() {
        return crc16First;
    }

    public Short getCrc16Second() {
        return crc16Second;
    }

}