package sample.Client.Controller;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import Client.StoreClientTCP;

import java.io.IOException;

public class Main extends Application {


    public static final int GROUP_NAME_MAX_LENGTH = 100;
    public static final int GROUP_DESCRIPTION_MAX_LENGTH = 255;


    public static Stage window;
    public static StoreClientTCP clientTCP;
    private static final int PORT = 3307;
    private static final int CLIENT_ID = 1;

    @Override
    public void start(Stage primaryStage) throws Exception{
        window = primaryStage;
        primaryStage.setMinWidth(750);
        primaryStage.setMinHeight(600);
        setupConnectionWithServer();
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
        primaryStage.setTitle("Stock application");
        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.show();
    }

    private void setupConnectionWithServer() throws InterruptedException {
        while (true) {
            try {
                clientTCP = new StoreClientTCP(CLIENT_ID, PORT);
                System.out.println("Connection has been established");
                break;
            } catch (IOException e) {
                System.err.println("The server is down");
                Thread.sleep(5000);
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
