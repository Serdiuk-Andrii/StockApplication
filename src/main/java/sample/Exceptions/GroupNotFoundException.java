package Exceptions;

public class GroupNotFoundException extends Exception{

    public GroupNotFoundException(String s) {
        super("Group " + s + " is not present in the database");
    }

}
