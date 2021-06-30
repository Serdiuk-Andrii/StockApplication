package sample.Client.Controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import Server.Group;
import javafx.stage.Stage;
import sample.Client.Model.UserDataDTO;

import java.net.URL;
import java.util.ResourceBundle;

public class AddGroupController implements Initializable {

    private String oldName;

    private String oldDescription;

    @FXML
    private TextField addGroupName;

    @FXML
    private TextArea addGroupDescription;

    @FXML
    private Button addGroupButton;


    @FXML
    private void addGroupButtonClicked() {
        Group group = new Group();
        if (oldName != null) {
            group.setName(oldName);
            String newName = addGroupName.getText();
            if (oldName.equals(newName))
                group.setNewName(null);
            else
                group.setNewName(newName);
        } else
            group.setName(addGroupName.getText());
        String newDescription = addGroupDescription.getText();
        if (oldDescription != null && oldDescription.equals(newDescription))
            group.setDescription(null);
        else
            group.setDescription(newDescription);
        Main.window.setUserData(group);
        Stage stage = (Stage) addGroupButton.getScene().getWindow();
        stage.close();
    }


    @FXML
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Object object = Main.window.getUserData();
        if (object != null) {

            Group group = (Group) object;
            addGroupName.setText(group.getName());
            addGroupDescription.setText(group.getDescription());
            addGroupButton.setText("Edit");
            oldName = group.getName();
            oldDescription = group.getDescription();
        }
    }
}
