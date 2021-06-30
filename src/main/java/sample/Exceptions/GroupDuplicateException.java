package Exceptions;

public class GroupDuplicateException extends Exception{

    public GroupDuplicateException(String s) {
        super("Group " + s +" is already in the database");
    }

}
