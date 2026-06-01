import javafx.application.Application;
import javafx.stage.Stage;

import database.DatabaseConnection;
import ui.LoginUI;

public class Main extends Application {

    @Override
    public void start(Stage stage) {

        LoginUI.show(stage);

    }

    public static void main(String[] args) {

        DatabaseConnection.connect();

        launch();

    }
}