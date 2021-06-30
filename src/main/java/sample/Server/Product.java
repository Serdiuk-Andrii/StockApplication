/* This class is a simple container for a product in the database used for json parsing
 * File: Product.java
 * Author: Serdiuk Andrii
 **/


package Server;

import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
public class Product  {
    private String groupName;
    private String name;
    private String newName;
    private String description;
    private String producer;
    private int amount;
    private double price;

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNewName(String newName) {
        this.newName = newName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setProducer(String producer) {
        this.producer = producer;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getName() {
        return name;
    }

    public String getNewName() {
        return newName;
    }

    public String getDescription() {
        return description;
    }

    public String getProducer() {
        return producer;
    }

    public int getAmount() {
        return amount;
    }

    public double getPrice() {
        return price;
    }

    public static final int PRODUCT_NAME_MAXIMUM_LENGTH = 100;
    public static final int PRODUCT_DESCRIPTION_MAXIMUM_LENGTH = 255;
    public static final int PRODUCT_PRODUCER_MAXIMUM_LENGTH = 70;

    public Product() {}

    public Product(String groupName, String name, String newName, String description, String producer, int amount, double price) {
        this.groupName = groupName;
        this.name = name;
        this.newName = newName;
        this.description = description;
        this.producer = producer;
        this.amount = amount;
        this.price = price;
    }
}
