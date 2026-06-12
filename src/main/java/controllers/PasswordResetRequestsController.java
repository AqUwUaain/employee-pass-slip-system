package controllers;

import database.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import models.PasswordResetRequest;
import utils.NavigationHelper;
import utils.PhilTime;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PasswordResetRequestsController {

    @FXML
    private TableView<PasswordResetRequest> requestsTable;

    @FXML
    private TableColumn<PasswordResetRequest, Number> colId;

    @FXML
    private TableColumn<PasswordResetRequest, String> colEmail;

    @FXML
    private TableColumn<PasswordResetRequest, String> colRequestedAt;

    @FXML
    private TableColumn<PasswordResetRequest, String> colStatus;

    @FXML
    private Button btnApprove;

    @FXML
    private Button btnReject;

    @FXML
    private Button btnSidebarDashboard;

    @FXML
    private Button btnSidebarMonitoring;

    @FXML
    private Button btnSidebarEmployees;

    @FXML
    private Button btnSidebarReports;

    @FXML
    private Button btnSidebarUsers;

    @FXML
    private Button btnSidebarPasswordReset;

    @FXML
    private Button btnLogout;

    @FXML
    private Label messageLabel;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @FXML
    private void initialize() {
        NavigationHelper.setActiveButton(btnSidebarPasswordReset);

        colId.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getId()));
        colEmail.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getEmail()));
        colRequestedAt.setCellValueFactory(cellData -> {
            Timestamp ts = cellData.getValue().getRequestedAt();
            String formatted = ts != null ? ts.toLocalDateTime().format(formatter) : "";
            return new javafx.beans.property.SimpleStringProperty(formatted);
        });
        colStatus.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStatus()));

        btnApprove.setOnAction(e -> updateRequestStatus("APPROVED"));
        btnReject.setOnAction(e -> updateRequestStatus("REJECTED"));

        if (btnSidebarDashboard != null)
            btnSidebarDashboard.setOnAction(e -> NavigationHelper.navigateToDashboard(btnSidebarDashboard));
        if (btnSidebarMonitoring != null)
            btnSidebarMonitoring.setOnAction(e -> NavigationHelper.navigateTo(btnSidebarMonitoring, "/fxml/Monitoring.fxml"));
        if (btnSidebarEmployees != null)
            btnSidebarEmployees.setOnAction(e -> NavigationHelper.navigateTo(btnSidebarEmployees, "/fxml/EmployeeController.fxml"));
        if (btnSidebarReports != null)
            btnSidebarReports.setOnAction(e -> NavigationHelper.navigateTo(btnSidebarReports, "/fxml/Reports.fxml"));
        if (btnSidebarUsers != null)
            btnSidebarUsers.setOnAction(e -> NavigationHelper.navigateTo(btnSidebarUsers, "/fxml/User.fxml"));
        if (btnSidebarPasswordReset != null)
            btnSidebarPasswordReset.setOnAction(e -> NavigationHelper.navigateTo(btnSidebarPasswordReset, "/fxml/PasswordResetRequests.fxml"));
        if (btnLogout != null)
            btnLogout.setOnAction(e -> NavigationHelper.logout(btnLogout));

        refreshRequests();
    }

    private void refreshRequests() {
        ObservableList<PasswordResetRequest> requests = getPendingRequests();
        requestsTable.setItems(requests);
    }

    private ObservableList<PasswordResetRequest> getPendingRequests() {
        ObservableList<PasswordResetRequest> requests = FXCollections.observableArrayList();
        try {
            Connection connection = DatabaseConnection.connect();
            String query = "SELECT * FROM password_reset_requests WHERE status = 'PENDING' AND (used = FALSE OR used IS NULL) ORDER BY requested_at DESC";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                requests.add(new PasswordResetRequest(
                    resultSet.getInt("id"),
                    resultSet.getString("email"),
                    resultSet.getTimestamp("requested_at"),
                    resultSet.getString("status"),
                    resultSet.getTimestamp("approved_at"),
                    resultSet.getBoolean("used")
                ));
            }

            statement.close();
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return requests;
    }

    private void updateRequestStatus(String status) {
        PasswordResetRequest selected = requestsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            messageLabel.setText("Select a request first.");
            messageLabel.setStyle("-fx-text-fill: #FCA5A5; -fx-font-weight: bold;");
            return;
        }

        try {
            Connection connection = DatabaseConnection.connect();
            String updateQuery;
            if ("APPROVED".equals(status)) {
                updateQuery = "UPDATE password_reset_requests SET status = ?, approved_at = ? WHERE id = ?";
            } else {
                updateQuery = "UPDATE password_reset_requests SET status = ? WHERE id = ?";
            }
            PreparedStatement statement = connection.prepareStatement(updateQuery);
            statement.setString(1, status);
            if ("APPROVED".equals(status)) {
                statement.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now(PhilTime.ZONE)));
                statement.setInt(3, selected.getId());
            } else {
                statement.setInt(2, selected.getId());
            }
            int updated = statement.executeUpdate();

            if (updated > 0) {
                messageLabel.setText("Request " + status.toLowerCase() + " successfully.");
                messageLabel.setStyle("-fx-text-fill: #34D399; -fx-font-weight: bold;");
                refreshRequests();
                ActivityLogController.logActivity("Password reset request " + status.toLowerCase() + " for: " + selected.getEmail(), 0);
            } else {
                messageLabel.setText("Failed to update request.");
                messageLabel.setStyle("-fx-text-fill: #FCA5A5; -fx-font-weight: bold;");
            }

            statement.close();
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("Database error: " + e.getMessage());
            messageLabel.setStyle("-fx-text-fill: #FCA5A5; -fx-font-weight: bold;");
        }
    }
}
