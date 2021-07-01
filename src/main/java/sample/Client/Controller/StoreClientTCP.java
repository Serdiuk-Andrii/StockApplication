/*
* File: StoreClientTCP.java
* Author: Serdiuk Andrii
* */

package Client;

import Exceptions.CryptoException;
import Exceptions.GroupDuplicateException;
import Exceptions.PacketDecodeException;
import Server.Group;
import Server.Message;
import Server.Packet;
import Server.Product;
import Utilities.CommandTypeEncoder;
import com.google.common.primitives.UnsignedLong;
import com.google.gson.Gson;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class StoreClientTCP {

    private final int clientId;
    private Socket socket;
    private final InputStream inputStream;
    private final OutputStream outputStream;
    private UnsignedLong packetId;
    private boolean receivedResponse = true;
    private final Gson jsonParser = new Gson();

    private static final byte APPLICATION_ID = 1;

    public StoreClientTCP(int clientId, int port) throws IOException {
        this.clientId = clientId;
        this.packetId = UnsignedLong.ZERO;
        socket = new Socket("localhost", port);
        socket.setSoTimeout(100000);
        inputStream = socket.getInputStream();
        outputStream = socket.getOutputStream();
    }

    public void sendPacket(final byte[] message) throws IOException, PacketDecodeException, CryptoException {

        outputStream.write(message);
        packetId = packetId.plus(UnsignedLong.ONE);
       /* byte[] buffer = new byte[Packet.MAX_SIZE];
        receiveAnswer(buffer);
        System.out.println("Client#" + clientId + " has received the answer");
        return new Packet(buffer);*/
    }



    // ----------------------- Group Methods ----------------------------- //


    public boolean addGroup(final String groupName, final String groupDescription) throws CryptoException, IOException, PacketDecodeException, GroupDuplicateException {
        Group group = new Group();
        group.setName(groupName);
        group.setDescription(groupDescription);
        sendGroupInformation(group, CommandTypeEncoder.GROUP_CREATE);
        return positiveResponseFromServer();
    }

    /**This method changes the name and the description of the group. All the needed checks
     * preventing duplicate group names have been already done
     * @param oldGroupName The old name of the group
     * @param newGroupName New name of the group, <code>null</code> if the name must remain the same
     * @param newGroupDescription New description, <code>null</code>if the description must remain the same
     * */
    public boolean updateGroup(final String oldGroupName, final String newGroupName,
                            final String newGroupDescription) throws IOException, CryptoException, PacketDecodeException {
        Group group = new Group();
        group.setName(oldGroupName);
        group.setDescription(newGroupDescription);
        group.setNewName(newGroupName);
        sendGroupInformation(group, CommandTypeEncoder.GROUP_UPDATE);
        return positiveResponseFromServer();
    }

    public List<Group> getGroupsWithProductsContainingSubstring(final String string) throws CryptoException, IOException, PacketDecodeException {
        final Message message = new Message(CommandTypeEncoder.GROUP_LIST_CONTAINING_STRING, clientId, string.getBytes(StandardCharsets.UTF_8));
        sendPacket(new Packet(APPLICATION_ID, packetId, message).convertToPacket());
        packetId = packetId.plus(UnsignedLong.ONE);
        final byte[] response = getResponseFromServer();
        Packet packet = new Packet(response);
        List<Group> list = new ArrayList<>();
        String groups = packet.getMessage().getMessageText();
        if (groups != null && !groups.isEmpty())
            fillInTheListOfGroups(list, groups);
        return list;
    }

    private void fillInTheListOfGroups(final List<Server.Group> list, final String messageText) {
        for(String s : messageText.split("\n")) {
            String[] values = s.split("@");
            Group group = new Group();
            group.setName(values[0]);
            group.setDescription(values[1]);
            list.add(group);
        }
    }

    public double getGlobalCost() throws CryptoException, IOException, PacketDecodeException {
        final Message message = new Message(CommandTypeEncoder.PRODUCT_COST, clientId, new byte[0]);
        sendPacket(new Packet(APPLICATION_ID, packetId, message).convertToPacket());
        packetId = packetId.plus(UnsignedLong.ONE);
        final byte[] response = getResponseFromServer();
        return Double.parseDouble(new Packet(response).getMessage().getMessageText());
    }

    public double getGroupCost(final String groupName) throws IOException, CryptoException, PacketDecodeException {
        Group group = new Group();
        group.setName(groupName);
        sendGroupInformation(group, CommandTypeEncoder.GROUP_COST);
        final byte[] buffer = new byte[Packet.MAX_SIZE];
        inputStream.read(buffer);
        Packet packet = new Packet(buffer);
        if(packet.getMessage().getCType() == CommandTypeEncoder.ERROR)
            return -1;
        return Double.parseDouble(packet.getMessage().getMessageText());
    }

    public boolean removeGroup(final String groupName) throws CryptoException, IOException, PacketDecodeException {
        //groupName has been already checked
        Group group = new Group();
        group.setName(groupName);
        sendGroupInformation(group, CommandTypeEncoder.GROUP_DELETE);
        return positiveResponseFromServer();
    }

    private byte[] requestAllGroups() throws CryptoException, IOException, PacketDecodeException {
        final Message message = new Message(CommandTypeEncoder.GROUP_LIST_ALL, clientId, new byte[0]);
        sendPacket(new Packet(APPLICATION_ID, packetId, message).convertToPacket());
        packetId = packetId.plus(UnsignedLong.ONE);
        return getResponseFromServer();
    }

    private void processEncodedGroupsInString(final String string, final List<Group> groups) {
        for (String s : string.split("\n")) {
            String[] values = s.split("@");
            Group group = new Group();
            group.setName(values[0]);
            group.setDescription(values[1]);
            groups.add(group);
        }
    }

    public List<Group> getAllGroups() throws IOException, PacketDecodeException, CryptoException {
        byte[] response = requestAllGroups();
        Packet packet = new Packet(response);
        String encodedGroups = packet.getMessage().getMessageText();
        List<Group> groups = new ArrayList<>();
        if (encodedGroups != null && !encodedGroups.isEmpty())
            processEncodedGroupsInString(encodedGroups, groups);
        return groups;
    }

    private void sendGroupInformation(final Group group, final int commandType) throws CryptoException, IOException, PacketDecodeException {
        final byte[] body = jsonParser.toJson(group, Group.class).getBytes(StandardCharsets.UTF_8);
        final Message message = new Message(commandType, clientId, body);
        sendPacket(new Packet(APPLICATION_ID, packetId, message).convertToPacket());
        packetId = packetId.plus(UnsignedLong.ONE);
    }



    // ----------------------- Group Methods ----------------------------- //



    // ----------------------- Product Methods ----------------------------- //


    public boolean addProduct(final Product product) throws IOException, CryptoException, PacketDecodeException {
        sendProductInformation(product, CommandTypeEncoder.PRODUCT_CREATE);
        return positiveResponseFromServer();
    }

    public boolean updateProduct(final Product product) throws IOException, CryptoException, PacketDecodeException {
        sendProductInformation(product, CommandTypeEncoder.PRODUCT_UPDATE);
        return positiveResponseFromServer();
    }

    public boolean removeProduct(final Product product) throws IOException, CryptoException, PacketDecodeException {
        sendProductInformation(product, CommandTypeEncoder.PRODUCT_DELETE);
        return positiveResponseFromServer();
    }

    private byte[] requestProducts(final String groupName) throws CryptoException, IOException, PacketDecodeException {
        final Message message = new Message(CommandTypeEncoder.GROUP_LIST_SPECIFIC, clientId, groupName.getBytes(StandardCharsets.UTF_8));
        sendPacket(new Packet(APPLICATION_ID, packetId, message).convertToPacket());
        packetId = packetId.plus(UnsignedLong.ONE);
        return getResponseFromServer();
    }

    public List<Product> getProducts(final String groupName) throws IOException, CryptoException, PacketDecodeException {
        final byte[] response = requestProducts(groupName);

        final String encodedProducts = new Packet(response).getMessage().getMessageText();
        final List<Product> products = new ArrayList<>();
        processEncodedProductsInStrings(encodedProducts, products);
        return products;
    }

    private void processEncodedProductsInStrings(String encodedProducts, List<Server.Product> products) {
        if (encodedProducts == null || encodedProducts.isEmpty())
            return;
        for (final String s : encodedProducts.split("\n")) {
            final String[] values = s.split("@");
            final Product product = new Product();
            product.setName(values[0]);
            product.setDescription(values[1]);
            product.setProducer(values[2]);
            product.setPrice(Double.parseDouble(values[3]));
            product.setAmount(Integer.parseInt(values[4]));
            products.add(product);
        }
    }

    private byte[] requestAllProducts() throws CryptoException, IOException, PacketDecodeException {
        final Message message = new Message(CommandTypeEncoder.PRODUCT_LIST_ALL, clientId, new byte[0]);
        sendPacket(new Packet(APPLICATION_ID, packetId, message).convertToPacket());
        packetId = packetId.plus(UnsignedLong.ONE);
        return getResponseFromServer();
    }

    public List<Product> getAllProducts() throws IOException, CryptoException, PacketDecodeException {
        final byte[] response = requestAllProducts();
        final Packet packet = new Packet(response);
        final String encodedProducts = packet.getMessage().getMessageText();
        final List<Product> products = new ArrayList<>();
        processEncodedProductsInStringsWithGroups(encodedProducts, products);
        return products;
    }

    private void processEncodedProductsInStringsWithGroups(final String string, final List<Product> products) {
        if (string == null || string.isEmpty())
            return;
        for (final String s : string.split("\n")) {
            final String[] values = s.split("@");
            final Product product = new Product();
            product.setName(values[0]);
            product.setGroupName(values[1]);
            product.setDescription(values[2]);
            product.setProducer(values[3]);
            product.setPrice(Double.parseDouble(values[4]));
            product.setAmount(Integer.parseInt(values[5]));
            products.add(product);
        }
    }




    private void sendProductInformation(final Product product, final int commandType) throws CryptoException, IOException, PacketDecodeException {
        final byte[] body = jsonParser.toJson(product, Product.class).getBytes(StandardCharsets.UTF_8);
        final Message message = new Message(commandType, clientId, body);
        sendPacket(new Packet(APPLICATION_ID, packetId, message).convertToPacket());
        packetId = packetId.plus(UnsignedLong.ONE);
    }



    // ----------------------- Product Methods ----------------------------- //

    private byte[] getResponseFromServer() throws IOException {
        final byte[] buffer = new byte[Packet.MAX_SIZE];
        inputStream.read(buffer);
        return buffer;
    }

    private boolean positiveResponseFromServer() throws IOException, PacketDecodeException, CryptoException {
        final byte[] buffer = getResponseFromServer();
        if(new Packet(buffer).getMessage().getCType() == CommandTypeEncoder.ERROR)
            return false;
        return true;
    }

    public void close() throws IOException {
        outputStream.close();
        inputStream.close();
        socket.close();
    }

    public boolean receivedResponse() {
        return receivedResponse;
    }

    public Packet sendPacketAndReceiveAnswer(byte[] message) throws IOException, PacketDecodeException, CryptoException {
        outputStream.write(message);
        byte[] buffer = new byte[Packet.MAX_SIZE];
        inputStream.read(buffer);
        System.out.println("Client#" + clientId + " has received the answer");
        return new Packet(buffer);
    }

    public int getClientId() {
        return clientId;
    }
/*
    public static void main(String[] args) throws IOException, CryptoException, PacketDecodeException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Type client`s id (client`s id must be unique):");
        byte clientId = Byte.parseByte(bufferedReader.readLine());
        StoreClientTCP client = new StoreClientTCP(clientId, Integer.parseInt(args[0]));
        while(true) {
            String s = bufferedReader.readLine();
            if (s.equals("send")) {
                Packet packet = client.sendPacketAndReceiveAnswer(PacketFactory.getPacket(client.clientId, client.packetId).convertToPacket());
                System.out.println(packet.getMessage().getMessageText());
            } else if(s.equals("stop"))
                break;
            else
                System.err.println("Unknown command");
        }
        client.close();
    }
*/
}
