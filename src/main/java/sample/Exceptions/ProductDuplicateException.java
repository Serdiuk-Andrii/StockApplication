package Exceptions;

public class ProductDuplicateException extends Exception{

    public ProductDuplicateException(String name) {
        super("Product " + name + " is already present in this table");
    }

}
