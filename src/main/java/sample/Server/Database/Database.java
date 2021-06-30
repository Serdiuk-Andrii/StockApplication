/*This class creates the connection with the database and performs simple queries.
 * File: Database.java
 * Author: Serdiuk Andrii
 * */

package Server.Database;

import Exceptions.GroupDuplicateException;
import Exceptions.GroupNotFoundException;
import Exceptions.ProductDuplicateException;
import Exceptions.ProductNotFoundException;
import Server.Product;

import java.sql.*;
import java.sql.Connection;

public class Database {

    public static final String DATABASE_NAME = "stock";
    private static final String TABLE_NAME = "all_products";
    private static final String LOGIN_TABLE_NAME = "login";
    private static final String GROUPS_TABLE_NAME = "stock_groups";
    private static final String CONNECTION_URL = "jdbc:mysql://localhost:3306/" + DATABASE_NAME;


    private static volatile Connection connection;
    private static final String GROUP_EXISTS_QUERY = "SELECT EXISTS(SELECT * FROM " + GROUPS_TABLE_NAME + " WHERE group_name = ?)"
            /*+ "LIMIT 1;"*/;
    private static final String PRODUCT_EXISTS_QUERY = "SELECT EXISTS(SELECT * FROM " + TABLE_NAME + " WHERE title = ?)";
    private static final String ADD_GROUP_TO_LIST_QUERY = "INSERT INTO " + GROUPS_TABLE_NAME + " (group_name, description) VALUES (?, ?)";

    private Database() {
        try {
            /*SQLiteConfig config = new SQLiteConfig();
            config.enforceForeignKeys(true);
            connection = DriverManager.getConnection(CONNECTION_URL, config.toProperties());*/
            //Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection(CONNECTION_URL, "root", "root");
            System.out.println("Connection has been established\n");
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    private static void createTable() {
        final String query = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME +
                " (title VARCHAR(100) PRIMARY KEY, description TEXT, producer TEXT, amount INTEGER, " +
                "price DOUBLE, group_name VARCHAR(100), FOREIGN KEY (group_name) REFERENCES " + GROUPS_TABLE_NAME
                + " (group_name) ON UPDATE CASCADE ON DELETE CASCADE" +
                ")";
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void createGroupsTable() {
        final String query = "CREATE TABLE IF NOT EXISTS " + GROUPS_TABLE_NAME +
                " (group_name VARCHAR(100) PRIMARY KEY, description TEXT)";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static boolean stringValueExists(String query, String value) {
        try {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, value);
            ResultSet set = statement.executeQuery();
            set.next();
            return set.getInt(1) != 0;
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
        return false;
    }


    //Product function start here


    public static void addProduct(final Product product) throws SQLException, GroupNotFoundException, ProductDuplicateException
    {addProduct(product.getGroupName(), product.getName(),
            product.getDescription(), product.getProducer(), product.getAmount(), product.getPrice());}

    public static void addProduct(final String groupName, final String productName, final String description,
                                  final String producer, final int amount, final double price) throws ProductDuplicateException, SQLException, GroupNotFoundException {
        if (amount < 0 || price < 0 || groupName == null || productName == null || description == null || producer == null)
            throw new IllegalArgumentException();
        if (!stringValueExists(GROUP_EXISTS_QUERY, groupName))
            throw new GroupNotFoundException(groupName);
        if (stringValueExists(PRODUCT_EXISTS_QUERY, productName))
            throw new ProductDuplicateException(productName);
        final String addProduct = "INSERT INTO " + TABLE_NAME +
                " (title, group_name, description, producer, amount, price) VALUES (?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement addProductStatement = connection.prepareStatement(addProduct);
            addProductStatement.setString(1, productName);
            addProductStatement.setString(2, groupName);
            addProductStatement.setString(3, description);
            addProductStatement.setString(4, producer);
            addProductStatement.setInt(5, amount);
            addProductStatement.setDouble(6, price);
            addProductStatement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void updateProduct(Product product) throws SQLException, ProductNotFoundException, ProductDuplicateException {
        updateProduct(product.getName(), product.getNewName(), product.getDescription(),
                product.getProducer(), product.getAmount(), product.getPrice());
    }

    /**This method updates the information inside the database about an existing object
     * @param oldProductName Current name of the product (a non-empty string)
     * @param newProductName New name of the product or null if it remains the same (a non-empty string)
     * @param newDescription New description of the product or null if it remains the same (a non-empty string)
     * @param newProducer New producer of the product or null if it remains the same (a non-empty string)
     * @param newAmount New amount of the product or -1 if it remains the same
     * @param newPrice New price of the product or -1 if it remains the same
     * */
    public static void updateProduct(final String oldProductName, final String newProductName,
                                     final String newDescription, final String newProducer, final int newAmount, final double newPrice) throws SQLException, ProductNotFoundException, ProductDuplicateException {
        validateString(oldProductName, "Illegal value for the product`s name");
        if (!stringValueExists(PRODUCT_EXISTS_QUERY, oldProductName))
            throw new ProductNotFoundException(oldProductName);
        final StringBuilder builder = new StringBuilder();
        builder.append(" SET ");
        boolean notFirstParameter = false;
        if (newProductName != null) {
            if (newProductName.isEmpty())
                throw new IllegalArgumentException("Empty product`s name");
            if (stringValueExists(PRODUCT_EXISTS_QUERY, newProductName))
                throw new ProductDuplicateException(newProductName);
            builder.append("title = ?");
            notFirstParameter = true;
        }
        if (newDescription != null) {
            if (newDescription.isEmpty())
                throw new IllegalArgumentException("Empty product`s description");
            appendCommaIfNecessary(notFirstParameter, builder);
            builder.append("description = ?");
            notFirstParameter = true;
        }
        if (newProducer != null) {
            if (newProducer.isEmpty())
                throw new IllegalArgumentException("Empty producer");
            appendCommaIfNecessary(notFirstParameter, builder);
            builder.append("producer = ?");
            notFirstParameter = true;
        }
        if (newAmount != -1) {
            if (newAmount < 0)
                throw new IllegalArgumentException("Negative amount");
            appendCommaIfNecessary(notFirstParameter, builder);
            builder.append("amount = ?");
            notFirstParameter = true;
        }
        if (newPrice != -1) {
            if (newPrice < 0)
                throw new IllegalArgumentException("Negative price");
            appendCommaIfNecessary(notFirstParameter, builder);
            builder.append("price = ?");
        }
        builder.append(" WHERE title = ?");
        final String firstQuery = "UPDATE " + TABLE_NAME + builder.toString();
        executeQuery(firstQuery, oldProductName, newProductName, newDescription, newProducer, newAmount, newPrice);
    }

    private static void executeQuery(String query, String oldProductName, String newProductName,
                                     String newDescription, String newProducer, int newAmount, double newPrice) throws SQLException {
        final PreparedStatement preparedStatement = connection.prepareStatement(query);
        byte currentIndex = 1;
        if (newProductName != null)
            preparedStatement.setString(currentIndex++, newProductName);
        if (newDescription != null)
            preparedStatement.setString(currentIndex++, newDescription);
        if (newProducer != null)
            preparedStatement.setString(currentIndex++, newProducer);
        if (newAmount != -1)
            preparedStatement.setInt(currentIndex++, newAmount);
        if (newPrice != -1)
            preparedStatement.setDouble(currentIndex++, newPrice);
        preparedStatement.setString(currentIndex, oldProductName);
        preparedStatement.executeUpdate();
    }

    private static void appendCommaIfNecessary(boolean value, final StringBuilder builder) {
        if (value)
            builder.append(", ");
    }

    public static Product readProduct(final String groupName, final String productName) throws SQLException, GroupNotFoundException, ProductNotFoundException {
        validateString(groupName, "Illegal group name");
        validateString(productName, "Illegal product name");
        if (!stringValueExists(GROUP_EXISTS_QUERY, groupName))
            throw new GroupNotFoundException(groupName);
        if (!stringValueExists(PRODUCT_EXISTS_QUERY, productName))
            throw new ProductNotFoundException(productName);
        final String query = "SELECT description, producer, amount, price FROM " + groupName + " WHERE title = ? LIMIT 1";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, productName);
        ResultSet set = preparedStatement.executeQuery();
        set.next();
        Product product = new Product();
        product.setGroupName(groupName);
        product.setName(productName);
        product.setDescription(set.getString("description"));
        product.setProducer(set.getString("producer"));
        product.setAmount(set.getInt("amount"));
        product.setPrice(set.getDouble("price"));
        return product;
    }

    public static void removeProduct(final String productName) throws ProductNotFoundException {
        validateString(productName, "Illegal product name");
        if (!stringValueExists(PRODUCT_EXISTS_QUERY, productName))
            throw new ProductNotFoundException(productName);
        final String query = "DELETE FROM " + TABLE_NAME + " WHERE title = ? LIMIT 1";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, productName);
            preparedStatement.execute();
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
    }


    public static ResultSet getAllProducts() throws SQLException {
        final String query = "SELECT * FROM " + TABLE_NAME;
        Statement statement = connection.createStatement();
        return statement.executeQuery(query);
    }

    //Product functions end here



    // Group functions start here

    /**This method creates a new group in the stock
     * @param groupName The name of the group, cannot be <code>null</code> or empty
     * @param description The group`s description, cannot be <code>null</code> or empty
     * */
    public static void addGroup(final String groupName, final String description) throws GroupDuplicateException {
        validateString(groupName, "Incorrect group name");
        validateString(description, "Incorrect description");
        if (stringValueExists(GROUP_EXISTS_QUERY, groupName))
            throw new GroupDuplicateException(groupName);
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(ADD_GROUP_TO_LIST_QUERY);
            preparedStatement.setString(1, groupName);
            preparedStatement.setString(2, description);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    /**This method changes the name and the description of the group. All the needed checks
     * preventing duplicate group names have been already done on the client`s side
     * @param oldGroupName The old name of the group
     * @param newGroupName New name of the group, <code>null</code> if the name must remain the same
     * @param newGroupDescription New description, <code>null</code>if the description must remain the same
     * */
    public static void updateGroup(final String oldGroupName, final String newGroupName, final String newGroupDescription) throws SQLException, GroupNotFoundException, GroupDuplicateException {
        validateString(oldGroupName, "Incorrect group name");
        if (!stringValueExists(GROUP_EXISTS_QUERY, oldGroupName))
            throw new GroupNotFoundException(oldGroupName);
        if (newGroupName == null && newGroupDescription != null)
            changeGroupDescription(oldGroupName, newGroupDescription);
        else {
            if (stringValueExists(GROUP_EXISTS_QUERY, newGroupName))
                throw new GroupDuplicateException(oldGroupName);
            if (newGroupDescription != null)
                changeGroupNameAndDescription(oldGroupName, newGroupName, newGroupDescription);
            else
                changeGroupName(oldGroupName, newGroupName);
        }
    }

    public static double getGroupCost(final String groupName) throws GroupNotFoundException, SQLException {
        validateString(groupName, "Incorrect group name");
        if (!stringValueExists(GROUP_EXISTS_QUERY, groupName))
            throw new GroupNotFoundException(groupName);
        double result = 0;
        String query = "SELECT amount, price FROM " + TABLE_NAME + " WHERE group_name = ?";
        PreparedStatement preparedStatement =connection.prepareStatement(query);
        preparedStatement.setString(1, groupName);
        ResultSet set = preparedStatement.executeQuery();
        while(set.next())
            result += set.getInt("amount")*set.getDouble("price");
        return result;
    }

    private static void changeGroupDescription(final String groupName, final String newGroupDescription) throws SQLException {
        final String query = "UPDATE " + GROUPS_TABLE_NAME + " SET description = ? WHERE group_name = ?";
        final PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, newGroupDescription);
        preparedStatement.setString(2, groupName);
        preparedStatement.executeUpdate();
    }

    private static void changeGroupName(final String oldGroupName, final String newGroupName) throws SQLException {
        final String query = "UPDATE " + GROUPS_TABLE_NAME + " SET group_name = ? WHERE group_name = ?";
        final PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, newGroupName);
        preparedStatement.setString(2, oldGroupName);
        preparedStatement.executeUpdate();
    }

    private static void changeGroupNameAndDescription(final String oldGroupName, final String newGroupName, final String newGroupDescription) throws SQLException {
        final String query = "UPDATE " + GROUPS_TABLE_NAME + " SET group_name = ?, description = ? WHERE group_name = ?";
        final PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, newGroupName);
        preparedStatement.setString(2, newGroupDescription);
        preparedStatement.setString(3, oldGroupName);
        preparedStatement.executeUpdate();
    }

    public static ResultSet readGroupDescription(final String groupName) throws SQLException, GroupNotFoundException {
        validateString(groupName, "Invalid group name");
        if (!stringValueExists(GROUP_EXISTS_QUERY, groupName))
            throw new GroupNotFoundException(groupName);
        final String query = "SELECT description FROM " + GROUPS_TABLE_NAME + " WHERE group_name = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, groupName);
        return preparedStatement.executeQuery();
    }

    public static void removeGroup(final String groupName) throws GroupNotFoundException, GroupDuplicateException {
        validateString(groupName, "Illegal group name");
        if (!stringValueExists(GROUP_EXISTS_QUERY, groupName))
            throw new GroupNotFoundException(groupName);
        final String deleteFromTheList = "DELETE FROM " + GROUPS_TABLE_NAME +  " WHERE group_name = ?";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(deleteFromTheList);
            preparedStatement.setString(1, groupName);
            preparedStatement.executeUpdate();
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }

    }

    public static ResultSet getProductsFromGroup(final String groupName) throws GroupNotFoundException {
        validateString(groupName, "Illegal group name");
        if (!stringValueExists(GROUP_EXISTS_QUERY, groupName))
            throw new GroupNotFoundException(groupName);
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE group_name = \"" + groupName + "\"";
        try {
            Statement statement = connection.createStatement();
            return statement.executeQuery(query);
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return null;
    }

    public static ResultSet getAllGroups() throws SQLException {
        String query =  "SELECT * FROM " + GROUPS_TABLE_NAME;
        Statement statement = connection.createStatement();
        return statement.executeQuery(query);
    }

    private static void validateString(final String string, final String exceptionText) {
        if(string == null || string.isEmpty())
            throw new IllegalArgumentException(exceptionText);
    }

    //Group functions end here

    public static Connection getConnection() {
        Connection result = connection;
        if (result != null)
            return result;
        synchronized(Database.class) {
            if (connection == null)
                new Database();
            return connection;
        }
    }

    public static void close() {
        try {
            connection.close();
            System.out.println("Connection has been closed");
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }





    public static void main(String[] args) throws GroupNotFoundException, GroupDuplicateException, InterruptedException, SQLException, ProductDuplicateException {
        Database.getConnection();
//        Database.registerUser("hello", "world");
        Database.createGroupsTable();
        Database.createTable();
//        Database.addGroup("rice", "good rice");
//        Database.addProduct("rice", "new rice", "r", "e", 32,32);
//        Database.removeGroup("rice");
        Database.close();

    }

}
