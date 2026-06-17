package controllers;

import database.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import utils.NavigationHelper;
import utils.Session;
import utils.SidebarHelper;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class SignatureController {

    @FXML
    private Button btnSidebarDashboard;

    @FXML
    private Button btnSidebarMonitoring;

    @FXML
    private Button btnSidebarEmployees;

    @FXML
    private Button btnSidebarReports;

    @FXML
    private Button btnSidebarLogReturn;

    @FXML
    private Button btnSidebarUsers;

    @FXML
    private Button btnSidebarSignatures;

    @FXML
    private Button btnSidebarPasswordReset;

    @FXML
    private Button btnLogout;

    @FXML
    private Button btnNotificationsAlert;

    @FXML
    private ImageView imgSignaturePreview;

    @FXML
    private Label lblSignatureStatus;

    @FXML
    private Label lblSignatureName;

    @FXML
    private Button btnUploadSignature;

    @FXML
    private Button btnRemoveSignature;

    @FXML
    private Button btnManageEmployees;

    @FXML
    private VBox manageEmployeesSubMenu;

    @FXML
    private void initialize() {
        SidebarHelper.initialize(
                btnSidebarDashboard, btnSidebarMonitoring,
                btnSidebarEmployees, btnSidebarReports,
                btnSidebarLogReturn, btnSidebarUsers,
                btnSidebarSignatures, btnSidebarPasswordReset,
                btnLogout, btnNotificationsAlert,
                btnSidebarSignatures
        );

        if (btnManageEmployees != null) {
            btnManageEmployees.setOnAction(event -> {
                boolean isVisible = manageEmployeesSubMenu.isVisible();
                manageEmployeesSubMenu.setVisible(!isVisible);
                manageEmployeesSubMenu.setManaged(!isVisible);
            });
        }

        if (manageEmployeesSubMenu != null) {
            manageEmployeesSubMenu.setVisible(true);
            manageEmployeesSubMenu.setManaged(true);
        }

        btnUploadSignature.setOnAction(event -> uploadSignature());
        btnRemoveSignature.setOnAction(event -> removeSignature());

        loadCurrentSignature();
    }

    private void loadCurrentSignature() {
        try (Connection connection = DatabaseConnection.connect()) {
            PreparedStatement stmt = connection.prepareStatement(
                    "SELECT signature_name, image_data FROM signatures WHERE user_id = ?"
            );
            stmt.setInt(1, Session.currentUserId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String name = rs.getString("signature_name");
                byte[] imageData = rs.getBytes("image_data");

                lblSignatureName.setText(name);
                lblSignatureStatus.setText("Signature on file");
                lblSignatureStatus.setStyle("-fx-text-fill: #34D399; -fx-font-weight: bold;");

                ByteArrayInputStream bis = new ByteArrayInputStream(imageData);
                Image fxImage = new Image(bis);
                imgSignaturePreview.setImage(fxImage);

                btnRemoveSignature.setVisible(true);
                btnRemoveSignature.setManaged(true);
            } else {
                lblSignatureName.setText("No signature uploaded");
                lblSignatureStatus.setText("Upload a signature to auto-sign pass slips");
                lblSignatureStatus.setStyle("-fx-text-fill: #A8A29E;");
                imgSignaturePreview.setImage(null);
                btnRemoveSignature.setVisible(false);
                btnRemoveSignature.setManaged(false);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void uploadSignature() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Signature Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.bmp")
        );

        File file = fileChooser.showOpenDialog(
                btnUploadSignature.getScene().getWindow()
        );

        if (file == null) return;

        try {
            FileInputStream fis = new FileInputStream(file);
            byte[] imageBytes = fis.readAllBytes();
            fis.close();

            try (Connection connection = DatabaseConnection.connect()) {

                PreparedStatement deleteStmt = connection.prepareStatement(
                        "DELETE FROM signatures WHERE user_id = ?"
                );
                deleteStmt.setInt(1, Session.currentUserId);
                deleteStmt.executeUpdate();
                deleteStmt.close();

                PreparedStatement stmt = connection.prepareStatement(
                        "INSERT INTO signatures (user_id, signature_name, image_data) VALUES (?, ?, ?)"
                );
                stmt.setInt(1, Session.currentUserId);
                stmt.setString(2, file.getName());
                stmt.setBytes(3, imageBytes);

                int inserted = stmt.executeUpdate();

                if (inserted > 0) {
                    lblSignatureName.setText(file.getName());
                    lblSignatureStatus.setText("Signature uploaded successfully");
                    lblSignatureStatus.setStyle("-fx-text-fill: #34D399; -fx-font-weight: bold;");

                    ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
                    Image fxImage = new Image(bis);
                    imgSignaturePreview.setImage(fxImage);

                    btnRemoveSignature.setVisible(true);
                    btnRemoveSignature.setManaged(true);

                    ActivityLogController.logActivity("Uploaded signature: " + file.getName(), 0);
                } else {
                    lblSignatureStatus.setText("FAILED TO UPLOAD SIGNATURE");
                    lblSignatureStatus.setStyle("-fx-text-fill: #FCA5A5; -fx-font-weight: bold;");
                }

                stmt.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            lblSignatureStatus.setText("ERROR: " + e.getMessage());
            lblSignatureStatus.setStyle("-fx-text-fill: #FCA5A5; -fx-font-weight: bold;");
        }
    }

    private void removeSignature() {
        try (Connection connection = DatabaseConnection.connect()) {
            PreparedStatement stmt = connection.prepareStatement(
                    "DELETE FROM signatures WHERE user_id = ?"
            );
            stmt.setInt(1, Session.currentUserId);

            int deleted = stmt.executeUpdate();

            if (deleted > 0) {
                lblSignatureName.setText("No signature uploaded");
                lblSignatureStatus.setText("Signature removed");
                lblSignatureStatus.setStyle("-fx-text-fill: #A8A29E;");
                imgSignaturePreview.setImage(null);
                btnRemoveSignature.setVisible(false);
                btnRemoveSignature.setManaged(false);

                ActivityLogController.logActivity("Removed signature", 0);
            }

            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static byte[] getSignatureByUserId(int userId) {
        try (Connection connection = DatabaseConnection.connect()) {
            PreparedStatement stmt = connection.prepareStatement(
                    "SELECT image_data FROM signatures WHERE user_id = ?"
            );
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            byte[] imageData = null;
            if (rs.next()) {
                imageData = rs.getBytes("image_data");
            }

            rs.close();
            stmt.close();
            return imageData;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
