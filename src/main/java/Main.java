import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import database.DatabaseConnection;
import database.DatabaseMigration;
import utils.ThemeManager;

public class  Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/Login.fxml")
        );

        Parent root = loader.load();

        Scene scene = new Scene(root);

        scene.getStylesheets().add(ThemeManager.getDarkCssPath());

        stage.setScene(scene);
        stage.setTitle("Employee Pass Slip System");
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.setWidth(1280);
        stage.setHeight(800);
        stage.show();
    }

    public static void main(String[] args) {

        Runtime.getRuntime().addShutdownHook(new Thread(DatabaseConnection::shutdown));
        DatabaseConnection.connect();
        DatabaseMigration.runMigrations();

        launch();
    }
}
