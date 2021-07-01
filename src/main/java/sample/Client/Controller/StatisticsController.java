package sample.Client.Controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;

import java.net.URL;
import java.util.ResourceBundle;
import Server.Group;
import lombok.SneakyThrows;

public class StatisticsController implements Initializable {

    @FXML
    private PieChart chart;


    @SneakyThrows
    @Override
    @FXML
    public void initialize(URL location, ResourceBundle resources) {
        for (Group group : Main.clientTCP.getAllGroups())
            chart.getData().add(new PieChart.Data(group.getName(), Main.clientTCP.getGroupCost(group.getName())));
    }
}
