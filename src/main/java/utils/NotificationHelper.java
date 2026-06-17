package utils;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import java.io.IOException;

public final class NotificationHelper {

    private NotificationHelper() {
    }

    private static final String OVERLAY_STYLE =
            "-fx-background-color: transparent;";

    private static final String POPUP_STYLE_DARK =
            "-fx-background-color: #252220;" +
            "-fx-background-radius: 16px;" +
            "-fx-border-color: #3D3229;" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 16px;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.5), 30, 0, 0, 8);";

    private static final String POPUP_STYLE_LIGHT =
            "-fx-background-color: #800517;" +
            "-fx-background-radius: 16px;" +
            "-fx-border-color: #6B0413;" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 16px;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 30, 0, 0, 8);";

    private static StackPane findRootOverlay(Node source) {
        Scene scene = source.getScene();
        if (scene == null) return null;
        Parent root = scene.getRoot();
        if (root instanceof StackPane stackPane) {
            return stackPane;
        }
        // Dynamically wrap non-StackPane roots so the overlay system works on all pages
        StackPane wrapper = new StackPane(root);
        wrapper.getStyleClass().add("notification-wrapper");
        scene.setRoot(wrapper);
        return wrapper;
    }

    private static StackPane getOrCreateOverlay(StackPane root) {
        for (javafx.scene.Node child : root.getChildren()) {
            if (child instanceof StackPane overlay
                    && overlay.getUserData() != null
                    && "notificationOverlay".equals(overlay.getUserData())) {
                return overlay;
            }
        }
        StackPane overlay = new StackPane();
        overlay.setUserData("notificationOverlay");
        overlay.setStyle(OVERLAY_STYLE);
        overlay.setVisible(false);
        overlay.setMouseTransparent(true);
        root.getChildren().add(overlay);
        StackPane.setAlignment(overlay, Pos.TOP_LEFT);
        StackPane.setMargin(overlay, Insets.EMPTY);
        return overlay;
    }

    public static void toggle(Node bellButton) {
        StackPane root = findRootOverlay(bellButton);
        if (root == null) return;

        StackPane overlay = getOrCreateOverlay(root);

        if (overlay.isVisible() && !overlay.getChildren().isEmpty()) {
            close(bellButton);
            return;
        }

        show(bellButton);
    }

    public static void show(Node bellButton) {
        StackPane root = findRootOverlay(bellButton);
        if (root == null) return;

        StackPane overlay = getOrCreateOverlay(root);

        if (overlay.isVisible() && !overlay.getChildren().isEmpty()) {
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    NotificationHelper.class.getResource("/fxml/NotificationPopup.fxml"));
            VBox popup = loader.load();

            popup.setPrefWidth(380);
            popup.setMaxWidth(380);
            popup.setMaxHeight(500);
            popup.setStyle(ThemeManager.isDark() ? POPUP_STYLE_DARK : POPUP_STYLE_LIGHT);

            Bounds bounds = bellButton.localToScreen(bellButton.getBoundsInLocal());
            if (bounds == null) return;

            double screenRight = bounds.getMaxX();
            double screenTop = bounds.getMaxY() + 5;

            Window window = bellButton.getScene().getWindow();
            double popupLeft = screenRight - 380 - window.getX();
            double popupTop = screenTop - window.getY();

            StackPane.setAlignment(popup, Pos.TOP_LEFT);
            StackPane.setMargin(popup, new Insets(popupTop, 0, 0, popupLeft));

            overlay.getChildren().add(popup);
            overlay.setVisible(true);
            overlay.setMouseTransparent(false);

            popup.setOnMouseClicked(e -> e.consume());

            overlay.setOnMouseClicked(e -> {
                if (e.getTarget() == overlay) {
                    close(bellButton);
                }
            });

            Window win = bellButton.getScene().getWindow();
            if (win instanceof javafx.stage.Stage stage) {
                stage.widthProperty().addListener((o, ov, nv) -> close(bellButton));
                stage.heightProperty().addListener((o, ov, nv) -> close(bellButton));
                stage.xProperty().addListener((o, ov, nv) -> close(bellButton));
                stage.yProperty().addListener((o, ov, nv) -> close(bellButton));
                stage.maximizedProperty().addListener((o, ov, nv) -> close(bellButton));
                stage.iconifiedProperty().addListener((o, ov, nv) -> close(bellButton));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void close(Node source) {
        StackPane root = findRootOverlay(source);
        if (root == null) return;

        for (javafx.scene.Node child : root.getChildren()) {
            if (child instanceof StackPane overlay
                    && "notificationOverlay".equals(overlay.getUserData())) {
                overlay.setVisible(false);
                overlay.setMouseTransparent(true);
                overlay.getChildren().clear();
                break;
            }
        }
    }
}
