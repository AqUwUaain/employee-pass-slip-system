import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import database.DatabaseConnection;
import database.DatabaseMigration;

public class  Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/Login.fxml")
        );

        Parent root = loader.load();

        Scene scene = new Scene(root);

        scene.getStylesheets().add(
                getClass().getResource("/css/style.css").toExternalForm()
        );

        stage.setScene(scene);
        stage.setTitle("Employee Pass Slip System");
        stage.show();
    }

    public static void main(String[] args) {

        DatabaseConnection.connect();
        DatabaseMigration.runMigrations();

        launch();
    }
}
