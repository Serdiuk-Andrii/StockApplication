package sample.Client.Controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    private static final String LOGIN = "root";
    private static final String PASSWORD = "root";

    @FXML
    private JFXTextField username;

    @FXML
    private JFXPasswordField password;

    @FXML
    private JFXButton loginButton;

    @FXML
    void login(MouseEvent event) throws IOException {
        if (username.getText().equals(LOGIN) && password.getText().equals(PASSWORD)) {
            Stage stage = (Stage)loginButton.getScene().getWindow();
            Parent myNewScene = FXMLLoader.load(getClass().getResource("/fxml/sample.fxml"));
            Scene scene = new Scene(myNewScene);
            stage.setScene(scene);
            stage.setTitle("Stock application");
            stage.show();
        } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Wrong Credentials");
                alert.setHeaderText("Wrong login or password");
                alert.showAndWait();
        }
    }

}
