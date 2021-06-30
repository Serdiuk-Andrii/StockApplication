package sample.Client.Model;

import Server.Group;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import sample.Interfaces.MyListener;

public class Item {

    @FXML
    private Label itemProductName;
    @FXML
    private Label itemCost;

    @FXML
    private Label itemAmount;

    private Group group;
    private MyListener listener;


    //Setting up the current group for the listener
    @FXML
    private void click(MouseEvent event) {
        listener.onClickListener(group);
    }

    public Label getItemProductName() {
        return itemProductName;
    }

    public void setItemProductName(Label itemProductName) {
        this.itemProductName = itemProductName;
    }

    public void setItemCost(Label itemCost) {
        this.itemCost = itemCost;
    }

    public void setItemAmount(Label itemProductsAmount) {
        this.itemAmount = itemProductsAmount;
    }

    public void setGroup(final Group group, final MyListener listener) {
        this.group = group;
        itemProductName.setText(group.getName());
        itemCost.setText(getGroupCost());
        itemAmount.setText(getProductsAmount());
        this.listener = listener;
    }

    public Label getItemCost() {
        return itemCost;
    }

    public Label getItemAmount() {
        return itemAmount;
    }

    public Group getGroup() {
        return group;
    }

    private String getGroupCost() {
        return "Cost";
    }

    private String getProductsAmount() {
        return "Amount";
    }

}
