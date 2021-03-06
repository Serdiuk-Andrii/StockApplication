package sample.Client.Controller;

import Exceptions.CryptoException;
import Exceptions.GroupDuplicateException;
import Exceptions.PacketDecodeException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import Server.Group;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.SneakyThrows;
import sample.Client.Model.Item;
import sample.Client.Model.UserDataDTO;
import sample.Interfaces.MyListener;

import java.io.IOException;
import java.net.URL;
import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Predicate;

public class Controller implements Initializable {

    @FXML
    private TextField searchBox;
    @FXML
    private Button searchButton;
    @FXML
    private VBox groupContainer;
    @FXML
    private Label groupName;
    @FXML
    private Label groupDescription;
    @FXML
    private Button groupOpenButton;

    @FXML
    private Button addGroupButton;

    @FXML
    private Button editGroupButton;

    @FXML
    private Button removeGroupButton;

    @FXML
    private GridPane grid;

    private int column = 0;
    private int row = 0;

    private static final int COLUMN_AMOUNT = 3;

    private Group currentSelectedGroup;

    private MyListener listener;
    private List<Group> groups = new ArrayList<Group>();


    private void getData() {
        try {
            groups.addAll(Main.clientTCP.getAllGroups());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (PacketDecodeException e) {
            e.printStackTrace();
        } catch (CryptoException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void addButtonOnClick() throws IOException {
        openGroupDialog();
        Group group =  (Group) Main.window.getUserData();
        if (group != null) {
            addGroup(group, true);
            Main.window.setUserData(null);
            groups.add(group);
        }
    }

    private void openGroupDialog() throws IOException {
        Stage stage = new Stage();
        stage.setResizable(true);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Add group");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/add_group.fxml"));
        Parent parent = loader.load();

        Scene scene = new Scene(parent);
        stage.setScene(scene);
        stage.showAndWait();
    }

    @FXML
    void editButtonOnClick(MouseEvent event) throws IOException, CryptoException, PacketDecodeException {
        Main.window.setUserData(currentSelectedGroup);
        openGroupDialog();
        Group group = (Group) Main.window.getUserData();
        if (group != null) {
            updateGroup(group);
            Main.window.setUserData(null);
        }
    }

    private void updateGroup(Group group) throws IOException, CryptoException, PacketDecodeException {
        if(!Main.clientTCP.updateGroup(group.getName(), group.getNewName(), group.getDescription())) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Group duplicate");
            alert.setHeaderText("Group " + group.getNewName() + " already exists!");
            alert.showAndWait();
        } else {
            Group toDisplay = new Group();
            if (group.getNewName() == null )
                toDisplay.setName(currentSelectedGroup.getName());
            else
                toDisplay.setName(group.getNewName());
            if (group.getDescription() == null)
                toDisplay.setDescription(currentSelectedGroup.getDescription());
            else
                toDisplay.setDescription(group.getDescription());
            updateGridPane(toDisplay);
        }
    }

    private void updateGridPane(Group group) throws IOException {
        groups.set(groups.indexOf(currentSelectedGroup), group);
        resetGroups();
        groupName.setText(group.getName());
        groupDescription.setText(group.getDescription());
        currentSelectedGroup = group;
    }


    @FXML
    void onRemoveButtonClicked(MouseEvent event) throws IOException, CryptoException, PacketDecodeException {
        if (currentSelectedGroup == null)
            return;
        removeProductFromGrid();
        Main.clientTCP.removeGroup(currentSelectedGroup.getName());
        removeGroupButton.setDisable(true);
        editGroupButton.setDisable(true);
    }

    private void removeProductFromGrid() throws IOException {
        groups.remove(currentSelectedGroup);
        resetGroups();
    }

    private void resetGroups() throws IOException {
        grid.getChildren().clear();
        column = 0;
        row = 0;
        for (Group g : groups)
            addGroup(g, false);
    }

    @FXML
    private void openGroup() throws IOException {
        Main.window.setUserData(currentSelectedGroup);
        Stage stage = new Stage();
        stage.setResizable(true);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(currentSelectedGroup.getName());
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/group.fxml"));
        Parent parent = loader.load();

        Scene scene = new Scene(parent);
        stage.setScene(scene);
        stage.showAndWait();
    }

    private void setChosenGroup(final Group group) {
        groupName.setText(group.getName());
        groupDescription.setText(group.getDescription());
        currentSelectedGroup = group;
        removeGroupButton.setDisable(false);
        editGroupButton.setDisable(false);
    }

    @SneakyThrows
    @FXML
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupDefaultConfiguration();
        AnchorPane pane = null;
        setupGridItemWidthAndHeight();
        try {
            for (int i = 0; i < groups.size(); i++)
                addGroup(groups.get(i), false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupDefaultConfiguration() {
        removeGroupButton.setDisable(true);
        editGroupButton.setDisable(true);
        getData();
        if (groups.size() > 0)
            setChosenGroup(groups.get(0));
        listener = new MyListener() {
            @Override
            public void onClickListener(Group group) {
                setChosenGroup(group);
            }
        };
    }


    private void setupGridItemWidthAndHeight() {
        grid.setMinWidth(Region.USE_COMPUTED_SIZE);
        grid.setPrefWidth(Region.USE_COMPUTED_SIZE);
        grid.setMaxWidth(Region.USE_PREF_SIZE);

        grid.setMinHeight(Region.USE_COMPUTED_SIZE);
        grid.setPrefHeight(Region.USE_COMPUTED_SIZE);
        grid.setMaxHeight(Region.USE_PREF_SIZE);
    }

    private boolean addToDatabase(final Group group) {
        try {
            if(!Main.clientTCP.addGroup(group.getName(), group.getDescription())) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Group duplicate");
                alert.setHeaderText("Group " + group.getName() + " already exists!");

                alert.showAndWait();
                return false;
            }
        } catch (CryptoException | GroupDuplicateException e) {
            e.printStackTrace();
        } catch (PacketDecodeException | IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    private void addGroup(final Group group, final boolean hasBeenJustCreated) throws IOException {
        if (hasBeenJustCreated)
            if(!addToDatabase(group))
                return;
        AnchorPane pane = addGridItem(group);
        pane.setUserData(group);
        if (column == COLUMN_AMOUNT) {
            column = 0;
            row++;
        }
        grid.add(pane, column++, row);
    }


    private AnchorPane addGridItem(final Group group) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("/fxml/item.fxml"));
        AnchorPane pane = fxmlLoader.load();
        Item item = fxmlLoader.getController();
        item.setGroup(group, listener);
        return pane;
    }

}