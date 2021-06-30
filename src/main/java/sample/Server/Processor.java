/* This file defines the class of a processor in a network which process a message in multiple threads
* File: Processor.java
* Author: Serdiuk Andrii
**/

package Server;
import Exceptions.GroupDuplicateException;
import Exceptions.GroupNotFoundException;
import sample.Interfaces.ConsciousRunnable;
import Server.Packet;
import Server.Message;
import Server.Group;
import Server.Product;
import Server.Database.Database;
import Utilities.CommandTypeEncoder;
import com.google.common.primitives.UnsignedLong;
import com.google.gson.Gson;

import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class Processor implements ConsciousRunnable {

    private static final Gson jsonParser = new Gson();
    private final BlockingQueue<Packet> messagesToProcess;
    private final BlockingQueue<Packet> responsesToEncrypt;
    private boolean isAlive;

    public Processor(BlockingQueue<Packet> messagesToProcess, BlockingQueue<Packet> responsesToEncrypt) {
        this.messagesToProcess = messagesToProcess;
        this.responsesToEncrypt = responsesToEncrypt;
    }


    private Message process (Message message) throws SQLException, GroupNotFoundException {
        int messageType = message.getCType();
        if (!CommandTypeEncoder.isProduct(message.getCType())) {
                if(messageType == CommandTypeEncoder.GROUP_CREATE) return handleCreateGroup(message);
                if(messageType == CommandTypeEncoder.GROUP_READ) return handleReadGroup(message);
                if(messageType == CommandTypeEncoder.GROUP_UPDATE) return handleUpdateGroup(message);
                if(messageType == CommandTypeEncoder.GROUP_DELETE) return handleDeleteGroup(message);
                if(messageType == CommandTypeEncoder.GROUP_LIST_ALL) return handleListAllGroups(message);
                if(messageType == CommandTypeEncoder.GROUP_COST) return handleGroupCost(message);
                if(messageType == CommandTypeEncoder.GROUP_LIST_SPECIFIC) return handleGetGroupProducts(message);
            try {
                return handleUnknownCommandType(message);
            } catch (GroupNotFoundException e) {
                e.printStackTrace();
            } catch (SQLException sqlException) {
                sqlException.printStackTrace();
            }
        } else {
                if(messageType == CommandTypeEncoder.PRODUCT_CREATE) return handleCreateProduct(message);
                if(messageType == CommandTypeEncoder.PRODUCT_READ) return handleReadProduct(message);
                if(messageType == CommandTypeEncoder.PRODUCT_UPDATE) return handleUpdateProduct(message);
                if(messageType == CommandTypeEncoder.PRODUCT_DELETE) return handleRemoveProduct(message);
                if(messageType == CommandTypeEncoder.PRODUCT_LIST_ALL) return handleListAllProducts(message);
                return handleUnknownCommandType(message);
        }
        return null;
    }

    private Message handleGetGroupProducts(Message message) throws GroupNotFoundException, SQLException {
        String groupName = message.getMessageText();
        ResultSet set = Database.getProductsFromGroup(groupName);
        StringBuilder builder = new StringBuilder(5000);
        while (set.next()) {
            builder.append(set.getString("title")).append(",");
            builder.append(set.getString("description")).append(",");
            builder.append(set.getString("producer")).append(",");
            builder.append(set.getDouble("price")).append(",");
            builder.append(set.getInt("amount")).append("\n");
        }
        byte[] body = builder.toString().getBytes(StandardCharsets.UTF_8);
        return new Message(message.getCType(), message.getBUserId(), body);
    }

    private Message handleGroupCost(Message message) {
        Group group = jsonParser.fromJson(message.getMessageText(), Group.class);
        byte[] response;
        int messageType = CommandTypeEncoder.ERROR;
        try {
            double cost = Database.getGroupCost(group.getName());
            response = String.valueOf(cost).getBytes(StandardCharsets.UTF_8);
            messageType = message.getCType();
        } catch (SQLException | GroupNotFoundException sqlException) {
            response = "An error occurred, try again".getBytes(StandardCharsets.UTF_8);
        }
        return new Message(messageType, message.getBUserId(), response);
    }

    private Message handleListAllProducts(Message message) throws SQLException {
        ResultSet set = Database.getAllProducts();
        StringBuilder builder = new StringBuilder(5000);
        while (set.next()) {
            builder.append(set.getString("title")).append(",");
            builder.append(set.getString("description")).append(",");
            builder.append(set.getString("producer")).append(",");
            builder.append(set.getDouble("price")).append(",");
            builder.append(set.getInt("amount")).append("\n");
        }
        byte[] body = builder.toString().getBytes(StandardCharsets.UTF_8);
        return new Message(message.getCType(), message.getBUserId(), body);
    }

    private Message handleRemoveProduct(Message message) {
        Product product = jsonParser.fromJson(message.getMessageText(), Product.class);
        byte[] response;
        int messageType = CommandTypeEncoder.ERROR;
        try {
            Database.removeProduct(product.getName());
            response = ("Product " + product.getName() + " has been removed").getBytes(StandardCharsets.UTF_8);
            messageType = message.getCType();
        } catch (Exception exception) {
            response = "An error occurred, try again".getBytes(StandardCharsets.UTF_8);
        }
        return new Message(messageType, message.getBUserId(), response);
    }

    private Message handleUpdateProduct(Message message) {
        Product product = jsonParser.fromJson(message.getMessageText(), Product.class);
        int messageType = CommandTypeEncoder.ERROR;
        byte[] response;
        try {
            Database.updateProduct(product);
            response = ("Product has been updated").getBytes(StandardCharsets.UTF_8);
            messageType = message.getCType();
        } catch (Exception exception) {
            response = "An error occurred, try again".getBytes(StandardCharsets.UTF_8);
            exception.printStackTrace();
        }
        return new Message(messageType, message.getBUserId(), response);
    }

    private Message handleReadProduct(Message message) {
        Product product = jsonParser.fromJson(message.getMessageText(), Product.class);
        byte[] response;
        int messageType = CommandTypeEncoder.ERROR;
        try {
            Database.readProduct(product.getGroupName(), product.getName());
            response = (jsonParser.toJson(product, Product.class)).getBytes(StandardCharsets.UTF_8);
            messageType = message.getCType();
        } catch (Exception exception) {
            response = "An error occurred, try again".getBytes(StandardCharsets.UTF_8);
        }
        return new Message(messageType, message.getBUserId(), response);
    }

    private Message handleCreateProduct(Message message) {
        Product product = jsonParser.fromJson(message.getMessageText(), Product.class);
        int messageType = CommandTypeEncoder.ERROR;
        byte[] response;
        try {
            Database.addProduct(product);
            response = ("Product " + product.getName() + " has been added").getBytes(StandardCharsets.UTF_8);
            messageType = message.getCType();
        } catch (Exception exception) {
            response = "An error occurred, try again".getBytes(StandardCharsets.UTF_8);
        }
        return new Message(messageType, message.getBUserId(), response);
    }

    private Message handleUnknownCommandType(Message message) throws GroupNotFoundException, SQLException {
        ResultSet set = Database.getProductsFromGroup(message.getMessageText());
        StringBuilder result = new StringBuilder();
        while(set.next()) {
            result.append(set.getString("title")).append(",");
            result.append(set.getString("description")).append(",");
            result.append(set.getString("producer")).append(",");
            result.append(set.getInt("amount")).append(",");
            result.append(set.getDouble("price")).append("\n");
        }
        return new Message(message.getCType(), message.getBUserId(), result.toString().getBytes(StandardCharsets.UTF_8));
    }

    private Message handleListAllGroups(Message message) {
        try {
            ResultSet set = Database.getAllGroups();
            StringBuilder builder = new StringBuilder(5000);
            while (set.next()) {
                builder.append(set.getString("group_name")).append(",");
                builder.append(set.getString("description")).append("\n");
            }
            byte[] body = builder.toString().getBytes(StandardCharsets.UTF_8);
            return new Message(message.getCType(), message.getBUserId(), body);
        } catch (SQLException throwables) {
            return new Message(message.getCType(), message.getBUserId(),
                    "An error occurred, try again".getBytes(StandardCharsets.UTF_8));
        }
    }

    private Message handleDeleteGroup(Message message) {
       final Group group = jsonParser.fromJson(message.getMessageText(), Group.class);
       try {
           Database.removeGroup(group.getName());
           return new Message(message.getCType(), message.getBUserId(),
                   ("Group " + group.getName() + " has been removed successfully").getBytes(StandardCharsets.UTF_8));
       } catch (GroupNotFoundException e) {
           return new Message(message.getCType(), message.getBUserId(),
                   "An error occurred, try again".getBytes(StandardCharsets.UTF_8));
       } catch (GroupDuplicateException e) {
           return new Message(message.getCType(), message.getBUserId(),
                   ("Group " + group.getName() + " was not found").getBytes(StandardCharsets.UTF_8));
       }
    }

    private Message handleUpdateGroup(Message message) {
        final Group group = jsonParser.fromJson(message.getMessageText(), Group.class);
        try {
            Database.updateGroup(group.getName(), group.getNewName() , group.getDescription());
            return new Message(message.getCType(), message.getBUserId(), "Group has been updated successfully".getBytes(StandardCharsets.UTF_8));
        } catch (SQLException sqlException) {
            return new Message(message.getCType(), message.getBUserId(),
                    "An error occurred, try again".getBytes(StandardCharsets.UTF_8));
        } catch (GroupNotFoundException e) {
            return new Message(CommandTypeEncoder.ERROR, message.getBUserId(),
                    ("Group " + group.getName() + " was not found").getBytes(StandardCharsets.UTF_8));
        } catch (GroupDuplicateException e) {
            return new Message(CommandTypeEncoder.ERROR, message.getBUserId(),
                    ("Group " + group.getName() + " is present in the database").getBytes(StandardCharsets.UTF_8));
        }
    }

    private Message handleReadGroup(Message message) {
        final Group group = jsonParser.fromJson(message.getMessageText(), Group.class);
        try {
            final ResultSet set = Database.readGroupDescription(group.getName());
            group.setDescription(set.getString("description"));
            final String messageBody = jsonParser.toJson(group, Group.class);
            return new Message(message.getCType(), message.getBUserId(), messageBody.getBytes(StandardCharsets.UTF_8));
        } catch (SQLException sqlException) {
            return new Message(message.getCType(), message.getBUserId(),
                    "An error occurred, try again".getBytes(StandardCharsets.UTF_8));
        } catch (GroupNotFoundException e) {
            return new Message(message.getCType(), message.getBUserId(),
                    ("Group " + group.getName() + " was not found").getBytes(StandardCharsets.UTF_8));
        }
    }

    private Message handleCreateGroup(Message message) {
        Group group = jsonParser.fromJson(message.getMessageText(), Group.class);
        try {
            Database.addGroup(group.getName(), group.getDescription());
        } catch (GroupDuplicateException e) {
            System.err.println("Duplicate group\n");
            return new Message(CommandTypeEncoder.ERROR, message.getBUserId(),
                    "Such a group already exists in the database".getBytes(StandardCharsets.UTF_8));
        }
        return new Message(message.getCType(), message.getBUserId(),
                ("Group " + group.getName() + "has been added successfully").getBytes(StandardCharsets.UTF_8));
    }


    @Override
    public void run() {
        isAlive = true;
        try {
            while(true) {
                Packet packet = messagesToProcess.take();
                //Checking whether the packet is a poison pill
                if (packet.getMessage() == null) {
                    responsesToEncrypt.add(packet);
                    break;
                }

                Message responseMessage = process(packet.getMessage());
                responsesToEncrypt.add(new Packet(packet.getbSrc(), packet.getbPktId(), responseMessage));
            }
        } catch(InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        } catch (GroupNotFoundException e) {
            e.printStackTrace();
        } finally {
            isAlive = false;
        }
    }

    public boolean isAlive() {
        return isAlive;
    }

}
