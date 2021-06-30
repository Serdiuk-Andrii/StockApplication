/* This class is a simple container for a group in the database used for json parsing
* File: Group.java
* Author: Serdiuk Andrii
**/


package Server;

public class Group {

    private String name;
    private String description;
    private String newName;

    public static final int GROUP_NAME_MAXIMUM_LENGTH = 30;
    public static final int GROUP_DESCRIPTION_MAXIMUM_LENGTH = 170;


    public Group() { }

    public Group(String name, String description, String newName) {
        this.name = name;
        this.description = description;
        this.newName = newName;
    }

    public void setNewName(String newName) {
        this.newName = newName;
    }

    public String getNewName() {
        return newName;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public boolean equals(Object that) {
        if (that == null) return false;
        if (that == this) return true;
        if (that.getClass() != this.getClass())
            return  false;
        Group group = (Group) that;
        return name.equals(((Group) that).getName());
    }
}
