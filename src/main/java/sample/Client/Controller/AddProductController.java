package sample.Client.Controller;

import Exceptions.CryptoException;
import Exceptions.PacketDecodeException;
import Server.Product;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AddProductController implements Initializable {

    private boolean isCreating;

    private String oldName, oldDescription, oldProducer;

    double oldPrice;

    int oldAmount;

    @FXML
    private TextField addProductName;

    @FXML
    private TextArea addProductDescription;

    @FXML
    private TextField addProductProducer;

    @FXML
    private TextField addProductPrice;

    @FXML
    private TextField addProductAmount;

    @FXML
    private Button addProductButton;

    @FXML
    void onCreateProductClicked(MouseEvent event) {
        if (inputDataIsCorrect()) {
            Product product = new Product();
            if (isCreating)
                setNewProductData(product);
            else
                updateProductData(product);
            Main.window.setUserData(product);
            Stage stage = (Stage) addProductButton.getScene().getWindow();
            stage.close();
        }
    }

    private void updateProductData(Product product) {
        String name = addProductName.getText();
        String description = addProductDescription.getText();
        String producer = addProductProducer.getText();
        int amount = Integer.parseInt(addProductAmount.getText());
        double price = Double.parseDouble(addProductPrice.getText());
        product.setName(oldName);
        product.setNewName(name.equals(oldName) ? null : name);
        product.setDescription(description.equals(oldDescription) ? null : description);
        product.setProducer(producer.equals(oldProducer) ? null : producer);
        product.setAmount(amount == oldAmount ? -1 : amount);
        product.setPrice(price == oldPrice ? -1 : price);
    }

    private void setNewProductData(Product product) {
        String groupName =  ((Server.Group) Main.window.getUserData()).getName();
        product.setGroupName(groupName);
        product.setName(addProductName.getText());
        product.setDescription(addProductDescription.getText());
        product.setProducer(addProductProducer.getText());
        product.setPrice(Double.parseDouble(addProductPrice.getText()));
        product.setAmount(Integer.parseInt(addProductAmount.getText()));
    }

    private boolean inputDataIsCorrect() {
        return true;
    }


    @Override
    @FXML
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (Main.window.getUserData().getClass() == Product.class) {
            Product product = (Product) Main.window.getUserData();
            oldName = product.getName();
            oldDescription = product.getDescription();
            oldProducer = product.getProducer();
            oldPrice = product.getPrice();
            oldAmount = product.getAmount();
            addProductName.setText(product.getName());
            addProductDescription.setText(product.getDescription());
            addProductProducer.setText(product.getProducer());
            addProductAmount.setText(String.valueOf(product.getAmount()));
            addProductPrice.setText(String.valueOf(product.getPrice()));
            addProductButton.setText("Change");
        } else
            isCreating = true;
    }
}
