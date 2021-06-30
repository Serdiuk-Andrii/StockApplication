package sample.Client.Controller;

import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.validation.RequiredFieldValidator;
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
    private JFXTextField addGroupName;

    @FXML
    private JFXTextArea addGroupDescription;

    @FXML
    private Button addGroupButton;


    @FXML
    private void addGroupButtonClicked() {
        if (addGroupName.validate() && addGroupDescription.validate()) {
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
    }


    @FXML
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initializeValidators();
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

    private void initializeValidators() {
        RequiredFieldValidator nameValidator = new RequiredFieldValidator();
        nameValidator.setMessage("Empty name");
        addGroupName.getValidators().add(nameValidator);
        RequiredFieldValidator descriptionValidator = new RequiredFieldValidator();
        descriptionValidator.setMessage("Empty description");
        addGroupDescription.getValidators().add(descriptionValidator);
    }
}
