import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import database.DatabaseConnection;

public class Main extends Application {

    @Override
    public void start(Stage stage) {



        Label label = new Label("EMPLOYEE PASS SLIP SYSTEM");

        Scene scene = new Scene(label, 500, 300);

        stage.setTitle("JavaFX Test");

        stage.setScene(scene);

        stage.show();

    }

    public static void main(String[] args) {

        DatabaseConnection.connect();

        launch();

    }

}