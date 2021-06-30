package sample.Client.Controller;

import Exceptions.CryptoException;
import Exceptions.PacketDecodeException;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;

import Server.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import Server.Product;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class GroupController implements Initializable {


    private Group currentGroup;

    @FXML
    private TextField searchFiled;

    @FXML
    private Button seacrhButton;

    @FXML
    private Button createProductButton;

    @FXML
    private Button removeProductButton;

    @FXML
    private Button editProductButton;

    @FXML
    private TableColumn<Product, String> productNameColumn;

    @FXML
    private TableColumn<Product, String> descriptionColumn;

    @FXML
    private TableColumn<Product, String> producerColumn;

    @FXML
    private TableColumn<Product, String> priceColumn;

    @FXML
    private TableColumn<Product, String> amountColumn;

    @FXML
    private TableView<Product> productsTableView;

    private final ObservableList<Product> observableList = FXCollections.observableArrayList();

    @Override
    @FXML
    public void initialize(URL url, ResourceBundle resourceBundle) {
        productsTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            editProductButton.setDisable(newSelection == null);
        });
        editProductButton.setDisable(true);
        currentGroup = (Group) Main.window.getUserData();
        productNameColumn.setCellValueFactory(new PropertyValueFactory<>("Name"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("Description"));
        producerColumn.setCellValueFactory(new PropertyValueFactory<>("Producer"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("Price"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("Amount"));
        try {
            getProductsFromDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getProductsFromDatabase() throws IOException, Exceptions.CryptoException, Exceptions.PacketDecodeException {
        List<Product> productList = Main.clientTCP.getProducts(currentGroup.getName());
        if (productList != null && !productList.isEmpty())
            observableList.addAll(productList);
        else
            editProductButton.setDisable(true);
        FilteredList<Product> list = new FilteredList<>(observableList, value -> true);
        searchFiled.textProperty().addListener((observable, oldValue, newValue) -> {
            list.setPredicate(value -> {
                if (newValue == null || newValue.isEmpty())
                    return true;
                String lowerCasedValue = newValue.toLowerCase();
                return value.getName().contains(lowerCasedValue);
            });
        } );
        SortedList<Product> sortedList = new SortedList<>(list);
        sortedList.comparatorProperty().bind(productsTableView.comparatorProperty());
        productsTableView.setItems(sortedList);
    }


    @FXML
    void onCreateProductClicked(MouseEvent event) throws IOException, CryptoException, PacketDecodeException {
        Main.window.setUserData(currentGroup);
        openProductDialogAndWait("Add product to " + currentGroup.getName());
        addProductIfCorrect();
    }

    private void addProductIfCorrect() throws IOException, CryptoException, PacketDecodeException {
        Object object = Main.window.getUserData();
        if (object != null && object.getClass() == Product.class) {
            Product product = (Product) object;
            addProduct(product);
            Main.window.setUserData(null);
        }

    }

    private void showDuplicateAlert(String productName) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Product duplicate");
        alert.setHeaderText("Such a product already exists!");

        alert.showAndWait();
        return;
    }

    private void addProduct(Server.Product product) throws IOException, CryptoException, PacketDecodeException {
        if (Main.clientTCP.addProduct(product)) {
            observableList.add(product);
            editProductButton.setDisable(false);
        }
        else
            showDuplicateAlert(product.getName());
    }


    @FXML
    void onRemoveButtonClicked(MouseEvent event) throws IOException, CryptoException, PacketDecodeException {
        Product product = productsTableView.getSelectionModel().getSelectedItem();
        if (product != null) {
            Main.clientTCP.removeProduct(product);
            observableList.remove(product);
            editProductButton.setDisable(observableList.isEmpty());
        }
    }

    @FXML
    void onEditProductButtonClicked(MouseEvent event) throws IOException, CryptoException, PacketDecodeException {
        Product product = productsTableView.getSelectionModel().getSelectedItem();
        Main.window.setUserData(product);
        openProductDialogAndWait("Change product " + product.getName());
        changeProductIfCorrect(product);
        Main.window.setUserData(null);
    }

    private void changeProductIfCorrect(Product oldProduct) throws IOException, CryptoException, PacketDecodeException {
        Product product = (Product) Main.window.getUserData();
        if (product != null) {
            if (Main.clientTCP.updateProduct(product)) {
                if (product.getNewName() != null)
                    oldProduct.setName(product.getNewName());
                if (product.getDescription() != null)
                    oldProduct.setDescription(product.getDescription());
                if (product.getProducer() != null)
                    oldProduct.setProducer(product.getProducer());
                if (product.getAmount() != -1)
                    oldProduct.setAmount(product.getAmount());
                if (product.getPrice() >= 0)
                    oldProduct.setPrice(product.getPrice());
                productsTableView.refresh();
            }
            else
                showDuplicateAlert(product.getName());
        }
    }


    private void openProductDialogAndWait(final String name) throws IOException {
        Stage stage = new Stage();
        stage.setResizable(true);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(name);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/add_product.fxml"));
        Parent parent = loader.load();

        Scene scene = new Scene(parent);
        stage.setScene(scene);
        stage.showAndWait();
    }

}
