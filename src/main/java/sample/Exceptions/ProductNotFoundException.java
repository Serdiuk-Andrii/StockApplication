package Exceptions;

public class ProductNotFoundException extends Exception {

    public ProductNotFoundException(String name) {
        super("Product " + name + " is not present in this table");
    }
    public ProductNotFoundException(int id) {super("Product with id " + id + " is not present in the table");}
}
