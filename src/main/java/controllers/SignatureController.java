package controllers;

import database.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import utils.NavigationHelper;
import utils.PhilTime;
import utils.Session;
import utils.SidebarHelper;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class SignatureController {

    @FXML
    private Button btnSidebarDashboard;

    @FXML
    private Button btnSidebarMonitoring;

    @FXML
    private Button btnSidebarEmployeeDirectory;

    @FXML
    private Button btnSidebarAddEmployee;

    @FXML
    private Button btnSidebarImportEmployee;

    @FXML
    private Button btnSidebarReports;

    @FXML
    private Button btnSidebarLogReturn;

    @FXML
    private Button btnSidebarUsers;

    @FXML
    private Button btnSidebarSignatures;

    @FXML
    private Button btnSidebarRequests;

    @FXML
    private Button btnLogout;

    @FXML
    private Button btnNotificationsAlert;

    @FXML
    private Button btnThemeToggle;

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
    private Button btnCancelRequest;

    @FXML
    private Button btnManageEmployees;

    @FXML
    private VBox manageEmployeesSubMenu;

    @FXML
    private VBox vboxRequestStatus;

    @FXML
    private Label lblRequestStatus;

    @FXML
    private Label lblRequestDate;

    private boolean isStaff = false;

    @FXML
    private void initialize() {
        SidebarHelper.initialize(
                btnSidebarDashboard, btnSidebarMonitoring,
                btnSidebarEmployeeDirectory, btnSidebarAddEmployee, btnSidebarImportEmployee,
                btnSidebarReports,
                btnSidebarLogReturn, btnSidebarUsers,
                btnSidebarSignatures, btnSidebarRequests,
                btnLogout, btnNotificationsAlert,
                btnSidebarSignatures, btnThemeToggle,
                btnManageEmployees, manageEmployeesSubMenu
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

        isStaff = "STAFF".equalsIgnoreCase(Session.currentRole);

        if (isStaff) {
            setupStaffMode();
        } else {
            setupAdminMode();
        }

        btnUploadSignature.setOnAction(event -> uploadSignature());
        btnRemoveSignature.setOnAction(event -> removeSignature());
        if (btnCancelRequest != null) {
            btnCancelRequest.setOnAction(event -> cancelSignatureRequest());
        }

        loadCurrentSignature();
    }

    private void setupStaffMode() {
        btnRemoveSignature.setText("Cancel Request");
        btnRemoveSignature.setVisible(false);
        btnRemoveSignature.setManaged(false);
    }

    private void setupAdminMode() {
        if (vboxRequestStatus != null) {
            vboxRequestStatus.setVisible(false);
            vboxRequestStatus.setManaged(false);
        }
    }

    private void loadCurrentSignature() {
        if (isStaff) {
            loadStaffSignatureStatus();
        } else {
            loadAdminSignature();
        }
    }

    private void loadAdminSignature() {
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

    private void loadStaffSignatureStatus() {
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
                lblSignatureStatus.setText("Signature approved and active");
                lblSignatureStatus.setStyle("-fx-text-fill: #34D399; -fx-font-weight: bold;");

                ByteArrayInputStream bis = new ByteArrayInputStream(imageData);
                Image fxImage = new Image(bis);
                imgSignaturePreview.setImage(fxImage);

                btnUploadSignature.setText("Update Signature");
                btnRemoveSignature.setVisible(false);
                btnRemoveSignature.setManaged(false);
            } else {
                stmt.close();
                rs.close();

                PreparedStatement reqStmt = connection.prepareStatement(
                        "SELECT id, signature_name, image_data, status, created_at FROM signature_requests WHERE user_id = ? ORDER BY created_at DESC LIMIT 1"
                );
                reqStmt.setInt(1, Session.currentUserId);
                ResultSet reqRs = reqStmt.executeQuery();

                if (reqRs.next()) {
                    String status = reqRs.getString("status");
                    String name = reqRs.getString("signature_name");
                    byte[] imageData = reqRs.getBytes("image_data");
                    Timestamp createdAt = reqRs.getTimestamp("created_at");

                    lblSignatureName.setText(name);
                    imgSignaturePreview.setImage(null);

                    if ("PENDING".equals(status)) {
                        lblSignatureStatus.setText("Request pending admin approval");
                        lblSignatureStatus.setStyle("-fx-text-fill: #FBBF24; -fx-font-weight: bold;");
                        btnUploadSignature.setDisable(true);
                        btnUploadSignature.setText("Request Pending...");
                        btnRemoveSignature.setVisible(true);
                        btnRemoveSignature.setManaged(true);
                    } else if ("REJECTED".equals(status)) {
                        lblSignatureStatus.setText("Request was rejected. You may submit a new one.");
                        lblSignatureStatus.setStyle("-fx-text-fill: #FCA5A5; -fx-font-weight: bold;");
                        btnUploadSignature.setDisable(false);
                        btnUploadSignature.setText("Upload Signature");
                        btnRemoveSignature.setVisible(false);
                        btnRemoveSignature.setManaged(false);
                    }

                    if (createdAt != null) {
                        lblRequestDate.setText("Submitted: " + createdAt.toLocalDateTime().format(
                                java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a")));
                    }

                    if (vboxRequestStatus != null) {
                        vboxRequestStatus.setVisible(true);
                        vboxRequestStatus.setManaged(true);
                    }
                } else {
                    lblSignatureName.setText("No signature uploaded");
                    lblSignatureStatus.setText("Upload a signature to auto-sign pass slips");
                    lblSignatureStatus.setStyle("-fx-text-fill: #A8A29E;");
                    imgSignaturePreview.setImage(null);
                    btnUploadSignature.setDisable(false);
                    btnUploadSignature.setText("Upload Signature");
                    btnRemoveSignature.setVisible(false);
                    btnRemoveSignature.setManaged(false);

                    if (vboxRequestStatus != null) {
                        vboxRequestStatus.setVisible(false);
                        vboxRequestStatus.setManaged(false);
                    }
                }

                reqStmt.close();
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

            if (isStaff) {
                submitSignatureRequest(file.getName(), imageBytes);
            } else {
                saveAdminSignature(file.getName(), imageBytes);
            }
        } catch (Exception e) {
            e.printStackTrace();
            lblSignatureStatus.setText("ERROR: " + e.getMessage());
            lblSignatureStatus.setStyle("-fx-text-fill: #FCA5A5; -fx-font-weight: bold;");
        }
    }

    private void submitSignatureRequest(String fileName, byte[] imageBytes) {
        try (Connection connection = DatabaseConnection.connect()) {
            PreparedStatement checkStmt = connection.prepareStatement(
                    "SELECT id, status FROM signature_requests WHERE user_id = ? AND status = 'PENDING'"
            );
            checkStmt.setInt(1, Session.currentUserId);
            ResultSet checkRs = checkStmt.executeQuery();

            if (checkRs.next()) {
                lblSignatureStatus.setText("You already have a pending request. Wait for admin approval.");
                lblSignatureStatus.setStyle("-fx-text-fill: #FBBF24; -fx-font-weight: bold;");
                checkStmt.close();
                return;
            }
            checkStmt.close();

            PreparedStatement deleteOldStmt = connection.prepareStatement(
                    "DELETE FROM signature_requests WHERE user_id = ? AND status != 'PENDING'"
            );
            deleteOldStmt.setInt(1, Session.currentUserId);
            deleteOldStmt.executeUpdate();
            deleteOldStmt.close();

            PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO signature_requests (user_id, signature_name, image_data, status, created_at) VALUES (?, ?, ?, 'PENDING', ?)"
            );
            stmt.setInt(1, Session.currentUserId);
            stmt.setString(2, fileName);
            stmt.setBytes(3, imageBytes);
            stmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now(PhilTime.ZONE)));

            int inserted = stmt.executeUpdate();

            if (inserted > 0) {
                lblSignatureName.setText(fileName);
                lblSignatureStatus.setText("Request submitted. Waiting for admin approval.");
                lblSignatureStatus.setStyle("-fx-text-fill: #FBBF24; -fx-font-weight: bold;");

                ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
                Image fxImage = new Image(bis);
                imgSignaturePreview.setImage(fxImage);

                btnUploadSignature.setDisable(true);
                btnUploadSignature.setText("Request Pending...");
                btnRemoveSignature.setVisible(true);
                btnRemoveSignature.setManaged(true);

                if (vboxRequestStatus != null) {
                    vboxRequestStatus.setVisible(true);
                    vboxRequestStatus.setManaged(true);
                    lblRequestDate.setText("Submitted: " + LocalDateTime.now(PhilTime.ZONE).format(
                            java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a")));
                }

                ActivityLogController.logActivity("Requested signature upload: " + fileName, 0);
            } else {
                lblSignatureStatus.setText("FAILED TO SUBMIT REQUEST");
                lblSignatureStatus.setStyle("-fx-text-fill: #FCA5A5; -fx-font-weight: bold;");
            }

            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
            lblSignatureStatus.setText("ERROR: " + e.getMessage());
            lblSignatureStatus.setStyle("-fx-text-fill: #FCA5A5; -fx-font-weight: bold;");
        }
    }

    private void saveAdminSignature(String fileName, byte[] imageBytes) {
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
            stmt.setString(2, fileName);
            stmt.setBytes(3, imageBytes);

            int inserted = stmt.executeUpdate();

            if (inserted > 0) {
                lblSignatureName.setText(fileName);
                lblSignatureStatus.setText("Signature uploaded successfully");
                lblSignatureStatus.setStyle("-fx-text-fill: #34D399; -fx-font-weight: bold;");

                ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
                Image fxImage = new Image(bis);
                imgSignaturePreview.setImage(fxImage);

                btnRemoveSignature.setVisible(true);
                btnRemoveSignature.setManaged(true);

                ActivityLogController.logActivity("Uploaded signature: " + fileName, 0);
            } else {
                lblSignatureStatus.setText("FAILED TO UPLOAD SIGNATURE");
                lblSignatureStatus.setStyle("-fx-text-fill: #FCA5A5; -fx-font-weight: bold;");
            }

            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
            lblSignatureStatus.setText("ERROR: " + e.getMessage());
            lblSignatureStatus.setStyle("-fx-text-fill: #FCA5A5; -fx-font-weight: bold;");
        }
    }

    private void removeSignature() {
        if (isStaff) {
            cancelSignatureRequest();
        } else {
            removeAdminSignature();
        }
    }

    private void cancelSignatureRequest() {
        try (Connection connection = DatabaseConnection.connect()) {
            PreparedStatement stmt = connection.prepareStatement(
                    "DELETE FROM signature_requests WHERE user_id = ? AND status = 'PENDING'"
            );
            stmt.setInt(1, Session.currentUserId);

            int deleted = stmt.executeUpdate();

            if (deleted > 0) {
                lblSignatureName.setText("No signature uploaded");
                lblSignatureStatus.setText("Request cancelled");
                lblSignatureStatus.setStyle("-fx-text-fill: #A8A29E;");
                imgSignaturePreview.setImage(null);
                btnUploadSignature.setDisable(false);
                btnUploadSignature.setText("Upload Signature");
                btnRemoveSignature.setVisible(false);
                btnRemoveSignature.setManaged(false);

                if (vboxRequestStatus != null) {
                    vboxRequestStatus.setVisible(false);
                    vboxRequestStatus.setManaged(false);
                }

                ActivityLogController.logActivity("Cancelled signature request", 0);
            }

            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void removeAdminSignature() {
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

    public static byte[] getLatestAdminSignature() {
        try (Connection connection = DatabaseConnection.connect()) {
            PreparedStatement stmt = connection.prepareStatement(
                    "SELECT s.image_data FROM signatures s JOIN users u ON s.user_id = u.id WHERE u.role = 'ADMIN' ORDER BY s.id DESC LIMIT 1"
            );
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
