package controllers;

import database.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import models.SignatureRequest;
import utils.NavigationHelper;
import utils.PhilTime;
import utils.Session;
import utils.SidebarHelper;

import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SignatureRequestsController {

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
    private Button btnManageEmployees;

    @FXML
    private VBox manageEmployeesSubMenu;

    @FXML
    private TableView<SignatureRequest> requestsTable;

    @FXML
    private TableColumn<SignatureRequest, Number> colId;

    @FXML
    private TableColumn<SignatureRequest, String> colStaffName;

    @FXML
    private TableColumn<SignatureRequest, String> colSignatureName;

    @FXML
    private TableColumn<SignatureRequest, String> colRequestedAt;

    @FXML
    private TableColumn<SignatureRequest, String> colStatus;

    @FXML
    private Label lblPreviewStaffName;

    @FXML
    private ImageView imgPreviewSignature;

    @FXML
    private Label lblPreviewStatus;

    @FXML
    private Button btnApprove;

    @FXML
    private Button btnReject;

    @FXML
    private Label messageLabel;

    @FXML
    private Label lblTotalPending;

    private ObservableList<SignatureRequest> requestsData;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss a");

    @FXML
    private void initialize() {
        SidebarHelper.initialize(
                btnSidebarDashboard, btnSidebarMonitoring,
                btnSidebarEmployeeDirectory, btnSidebarAddEmployee, btnSidebarImportEmployee,
                btnSidebarReports,
                btnSidebarLogReturn, btnSidebarUsers,
                btnSidebarSignatures, btnSidebarRequests,
                btnLogout, btnNotificationsAlert,
                btnSidebarRequests, btnThemeToggle,
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

        setupTableColumns();

        requestsTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showSignaturePreview(newValue)
        );

        btnApprove.setOnAction(e -> processRequest("APPROVED"));
        btnReject.setOnAction(e -> processRequest("REJECTED"));

        loadRequestsAsync();
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getId()));

        colStaffName.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStaffName()));

        colSignatureName.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getSignatureName()));

        colRequestedAt.setCellValueFactory(cellData -> {
            Timestamp ts = cellData.getValue().getCreatedAt();
            String formatted = ts != null ? ts.toLocalDateTime().format(formatter) : "";
            return new javafx.beans.property.SimpleStringProperty(formatted);
        });

        colStatus.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStatus()));

        colStatus.setCellFactory(col -> new javafx.scene.control.TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    String color = switch (item) {
                        case "PENDING" -> "#FBBF24";
                        case "APPROVED" -> "#34D399";
                        case "REJECTED" -> "#EF4444";
                        default -> "#A8A29E";
                    };
                    setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
                }
            }
        });
    }

    private void showSignaturePreview(SignatureRequest request) {
        if (request == null) {
            lblPreviewStaffName.setText("Select a request to preview");
            imgPreviewSignature.setImage(null);
            btnApprove.setDisable(true);
            btnReject.setDisable(true);
            return;
        }

        lblPreviewStaffName.setText(request.getStaffName());
        lblPreviewStatus.setText("Status: " + request.getStatus());
        lblPreviewStatus.setStyle("-fx-text-fill: " + ("PENDING".equals(request.getStatus()) ? "#FBBF24" : "#34D399") + "; -fx-font-weight: bold;");

        if (request.getImageData() != null) {
            ByteArrayInputStream bis = new ByteArrayInputStream(request.getImageData());
            Image fxImage = new Image(bis);
            imgPreviewSignature.setImage(fxImage);
        } else {
            imgPreviewSignature.setImage(null);
        }

        boolean isPending = "PENDING".equals(request.getStatus());
        btnApprove.setDisable(!isPending);
        btnReject.setDisable(!isPending);
    }

    private void loadRequestsAsync() {
        Thread thread = new Thread(() -> {
            ObservableList<SignatureRequest> requests = fetchRequests();
            javafx.application.Platform.runLater(() -> {
                requestsData = requests;
                requestsTable.setItems(requestsData);
                updatePendingCount();
            });
        });
        thread.setDaemon(true);
        thread.start();
    }

    private ObservableList<SignatureRequest> fetchRequests() {
        ObservableList<SignatureRequest> data = FXCollections.observableArrayList();

        try (Connection connection = DatabaseConnection.connect()) {
            String query = """
                    SELECT sr.id, sr.user_id, u.username AS staff_name, sr.signature_name,
                           sr.image_data, sr.status, sr.created_at
                    FROM signature_requests sr
                    JOIN users u ON sr.user_id = u.id
                    ORDER BY sr.created_at DESC
                    """;

            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                SignatureRequest request = new SignatureRequest(
                        resultSet.getInt("id"),
                        resultSet.getInt("user_id"),
                        resultSet.getString("staff_name"),
                        resultSet.getString("signature_name"),
                        resultSet.getBytes("image_data"),
                        resultSet.getString("status"),
                        resultSet.getTimestamp("created_at")
                );
                data.add(request);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }

    private void updatePendingCount() {
        long pendingCount = requestsData.stream()
                .filter(r -> "PENDING".equals(r.getStatus()))
                .count();
        lblTotalPending.setText(String.valueOf(pendingCount));
    }

    private void processRequest(String newStatus) {
        SignatureRequest selected = requestsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            messageLabel.setText("Select a request first.");
            messageLabel.setStyle("-fx-text-fill: #FCA5A5; -fx-font-weight: bold;");
            return;
        }

        if (!"PENDING".equals(selected.getStatus())) {
            messageLabel.setText("This request has already been processed.");
            messageLabel.setStyle("-fx-text-fill: #FCA5A5; -fx-font-weight: bold;");
            return;
        }

        try (Connection connection = DatabaseConnection.connect()) {
            if ("APPROVED".equals(newStatus)) {
                PreparedStatement deleteOld = connection.prepareStatement(
                        "DELETE FROM signatures WHERE user_id = ?"
                );
                deleteOld.setInt(1, selected.getUserId());
                deleteOld.executeUpdate();
                deleteOld.close();

                PreparedStatement insertSig = connection.prepareStatement(
                        "INSERT INTO signatures (user_id, signature_name, image_data) VALUES (?, ?, ?)"
                );
                insertSig.setInt(1, selected.getUserId());
                insertSig.setString(2, selected.getSignatureName());
                insertSig.setBytes(3, selected.getImageData());
                insertSig.executeUpdate();
                insertSig.close();
            }

            PreparedStatement updateStmt = connection.prepareStatement(
                    "UPDATE signature_requests SET status = ?, reviewed_by = ?, reviewed_at = ? WHERE id = ?"
            );
            updateStmt.setString(1, newStatus);
            updateStmt.setInt(2, Session.currentUserId);
            updateStmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now(PhilTime.ZONE)));
            updateStmt.setInt(4, selected.getId());

            int updated = updateStmt.executeUpdate();

            if (updated > 0) {
                messageLabel.setText("Request " + newStatus.toLowerCase() + " successfully.");
                messageLabel.setStyle("-fx-text-fill: #34D399; -fx-font-weight: bold;");

                ActivityLogController.logActivity(
                        "Signature request " + newStatus.toLowerCase() + " for: " + selected.getStaffName(),
                        selected.getUserId()
                );

                loadRequestsAsync();
            } else {
                messageLabel.setText("Failed to update request.");
                messageLabel.setStyle("-fx-text-fill: #FCA5A5; -fx-font-weight: bold;");
            }

            updateStmt.close();

        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("Database error: " + e.getMessage());
            messageLabel.setStyle("-fx-text-fill: #FCA5A5; -fx-font-weight: bold;");
        }
    }

}
