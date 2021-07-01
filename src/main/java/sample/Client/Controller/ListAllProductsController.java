package sample.Client.Controller;

import com.jfoenix.controls.JFXTextField;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import Server.Product;
import javafx.scene.control.cell.PropertyValueFactory;

public class ListAllProductsController implements Initializable {

    @FXML
    private TableView<Product> allProducts;

    @FXML
    private TableColumn<Product, String> productNameColumn;

    @FXML
    private TableColumn<Product, String> groupNameColumn;

    @FXML
    private TableColumn<Product, String> producerColumn;

    @FXML
    private TableColumn<Product, String> amountColumn;

    @FXML
    private TableColumn<Product, String> priceColumn;

    @FXML
    private TableColumn<Product, String> descriptionColumn;

    private final ObservableList<Product> observableList = FXCollections.observableArrayList();

    @FXML
    private JFXTextField searchField;

    @FXML
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        productNameColumn.setCellValueFactory(new PropertyValueFactory<>("Name"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("Description"));
        groupNameColumn.setCellValueFactory(new PropertyValueFactory<>("groupName"));
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
        List<Product> products = Main.clientTCP.getAllProducts();
        if (products != null)
            observableList.addAll(products);
        FilteredList<Product> list = new FilteredList<>(observableList, value -> true);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            list.setPredicate(value -> {
                if (newValue == null || newValue.isEmpty())
                    return true;
                String lowerCasedValue = newValue.toLowerCase();
                return value.getName().contains(lowerCasedValue);
            });
        } );
        SortedList<Product> sortedList = new SortedList<>(list);
        sortedList.comparatorProperty().bind(allProducts.comparatorProperty());
        allProducts.setItems(sortedList);
    }
}
