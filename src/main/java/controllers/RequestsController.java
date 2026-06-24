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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import models.PassSlip;
import models.PasswordResetRequest;
import models.SignatureRequest;
import models.UnifiedRequest;
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

public class RequestsController {

    @FXML private Button btnSidebarDashboard;
    @FXML private Button btnSidebarMonitoring;
    @FXML private Button btnSidebarEmployeeDirectory;
    @FXML private Button btnSidebarAddEmployee;
    @FXML private Button btnSidebarImportEmployee;
    @FXML private Button btnSidebarReports;
    @FXML private Button btnSidebarLogReturn;
    @FXML private Button btnSidebarUsers;
    @FXML private Button btnSidebarSignatures;
    @FXML private Button btnSidebarRequests;
    @FXML private Button btnLogout;
    @FXML private Button btnNotificationsAlert;
    @FXML private Button btnThemeToggle;
    @FXML private Button btnManageEmployees;
    @FXML private VBox manageEmployeesSubMenu;

    @FXML private TableView<UnifiedRequest> requestsTable;
    @FXML private TableColumn<UnifiedRequest, Number> colId;
    @FXML private TableColumn<UnifiedRequest, String> colCategory;
    @FXML private TableColumn<UnifiedRequest, String> colRequester;
    @FXML private TableColumn<UnifiedRequest, String> colDetail;
    @FXML private TableColumn<UnifiedRequest, String> colRequestedAt;
    @FXML private TableColumn<UnifiedRequest, String> colStatus;

    @FXML private Button btnApprove;
    @FXML private Button btnReject;
    @FXML private Button btnCancel;
    @FXML private Label messageLabel;
    @FXML private Label lblTotalPending;

    @FXML private VBox previewPane;
    @FXML private Label lblPreviewTitle;
    @FXML private Label lblPreviewRequester;
    @FXML private Label lblPreviewDetail;
    @FXML private Label lblPreviewDetail2;
    @FXML private Label lblPreviewDetail3;
    @FXML private Label lblPreviewStatus;
    @FXML private ImageView imgPreviewSignature;
    @FXML private VBox vboxSignaturePreview;
    @FXML private VBox vboxPassSlipPreview;

    @FXML private HBox filterBox;

    private ObservableList<UnifiedRequest> allRequests;
    private String currentFilter = "ALL";
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a");

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

        setupTableColumns();
        setupFilters();

        requestsTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showPreview(newValue)
        );

        btnApprove.setOnAction(e -> processSelected("APPROVED"));
        btnReject.setOnAction(e -> processSelected("REJECTED"));
        btnCancel.setOnAction(e -> processSelected("CANCELLED"));

        btnApprove.setDisable(true);
        btnReject.setDisable(true);
        btnCancel.setDisable(true);

        loadAllRequestsAsync();
    }

    private void setupFilters() {
        boolean isDark = utils.ThemeManager.isDark();
        String activeColor = isDark ? "#D4A853" : "#800517";
        String inactiveColor = isDark ? "#A8A29E" : "#6B7280";

        String[] filters = {"ALL", "PASSWORD RESET", "PASS SLIP", "SIGNATURE"};
        filterBox.getChildren().clear();

        for (String f : filters) {
            Label label = new Label(f);
            boolean isActive = f.equals(currentFilter);
            label.setStyle("-fx-text-fill: " + (isActive ? activeColor : inactiveColor)
                    + "; -fx-cursor: hand; -fx-font-size: 12px;"
                    + (isActive ? " -fx-font-weight: bold;" : ""));
            label.setOnMouseClicked(e -> {
                currentFilter = f;
                setupFilters();
                applyFilter();
            });
            filterBox.getChildren().add(label);
        }
    }

    private void applyFilter() {
        if (allRequests == null) return;
        if ("ALL".equals(currentFilter)) {
            requestsTable.setItems(allRequests);
        } else {
            UnifiedRequest.Category cat = UnifiedRequest.Category.valueOf(currentFilter.replace(" ", "_"));
            ObservableList<UnifiedRequest> filtered = FXCollections.observableArrayList();
            for (UnifiedRequest r : allRequests) {
                if (r.getCategory() == cat) filtered.add(r);
            }
            requestsTable.setItems(filtered);
        }
        updatePendingCount();
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getId()));

        colCategory.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCategory().name()));

        colCategory.setCellFactory(col -> new javafx.scene.control.TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    String color = switch (item) {
                        case "PASSWORD_RESET" -> "#60A5FA";
                        case "PASS_SLIP" -> "#FBBF24";
                        case "SIGNATURE" -> "#A78BFA";
                        default -> "#A8A29E";
                    };
                    setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
                }
            }
        });

        colRequester.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getRequester()));

        colDetail.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDetail()));

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
                        case "APPROVED", "OUT" -> "#34D399";
                        case "REJECTED" -> "#EF4444";
                        case "RETURNED" -> "#34D399";
                        case "CANCELLED" -> "#9CA3AF";
                        default -> "#A8A29E";
                    };
                    setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
                }
            }
        });
    }

    private void showPreview(UnifiedRequest request) {
        if (request == null) {
            clearPreview();
            return;
        }

        lblPreviewTitle.setText(request.getCategory().name().replace("_", " ") + " REQUEST");
        lblPreviewRequester.setText("Requester: " + request.getRequester());
        lblPreviewDetail.setText("Detail: " + request.getDetail());
        lblPreviewDetail2.setText("Date: " + (request.getCreatedAt() != null
                ? request.getCreatedAt().toLocalDateTime().format(formatter) : "N/A"));
        lblPreviewStatus.setText("Status: " + request.getStatus());

        String statusColor = switch (request.getStatus()) {
            case "PENDING", "OUT" -> "#FBBF24";
            case "APPROVED", "RETURNED" -> "#34D399";
            case "REJECTED" -> "#EF4444";
            case "CANCELLED" -> "#9CA3AF";
            default -> "#A8A29E";
        };
        lblPreviewStatus.setStyle("-fx-text-fill: " + statusColor + "; -fx-font-weight: bold; -fx-font-size: 13px;");

        vboxSignaturePreview.setVisible(false);
        vboxSignaturePreview.setManaged(false);
        vboxPassSlipPreview.setVisible(false);
        vboxPassSlipPreview.setManaged(false);
        lblPreviewDetail3.setVisible(false);
        lblPreviewDetail3.setManaged(false);

        boolean isPending = "PENDING".equals(request.getStatus());

        switch (request.getCategory()) {
            case PASSWORD_RESET -> {
                PasswordResetRequest prr = request.getRawData(PasswordResetRequest.class);
                btnApprove.setDisable(!isPending);
                btnReject.setDisable(!isPending);
                btnCancel.setDisable(true);
            }
            case PASS_SLIP -> {
                PassSlip ps = request.getRawData(PassSlip.class);
                vboxPassSlipPreview.setVisible(true);
                vboxPassSlipPreview.setManaged(true);
                lblPreviewDetail3.setVisible(true);
                lblPreviewDetail3.setManaged(true);
                lblPreviewDetail3.setText("Reason: " + (ps.getReason() != null ? ps.getReason() : "N/A"));
                btnApprove.setDisable(!isPending);
                btnReject.setDisable(!isPending);
                btnCancel.setDisable(!isPending);
            }
            case SIGNATURE -> {
                SignatureRequest sr = request.getRawData(SignatureRequest.class);
                vboxSignaturePreview.setVisible(true);
                vboxSignaturePreview.setManaged(true);
                if (sr.getImageData() != null) {
                    ByteArrayInputStream bis = new ByteArrayInputStream(sr.getImageData());
                    imgPreviewSignature.setImage(new Image(bis));
                } else {
                    imgPreviewSignature.setImage(null);
                }
                btnApprove.setDisable(!isPending);
                btnReject.setDisable(!isPending);
                btnCancel.setDisable(true);
            }
        }
    }

    private void clearPreview() {
        lblPreviewTitle.setText("Select a request");
        lblPreviewRequester.setText("");
        lblPreviewDetail.setText("");
        lblPreviewDetail2.setText("");
        lblPreviewDetail3.setText("");
        lblPreviewStatus.setText("");
        imgPreviewSignature.setImage(null);
        vboxSignaturePreview.setVisible(false);
        vboxSignaturePreview.setManaged(false);
        vboxPassSlipPreview.setVisible(false);
        vboxPassSlipPreview.setManaged(false);
        lblPreviewDetail3.setVisible(false);
        lblPreviewDetail3.setManaged(false);
        btnApprove.setDisable(true);
        btnReject.setDisable(true);
        btnCancel.setDisable(true);
    }

    private void loadAllRequestsAsync() {
        Thread thread = new Thread(() -> {
            ObservableList<UnifiedRequest> requests = fetchAllRequests();
            javafx.application.Platform.runLater(() -> {
                allRequests = requests;
                applyFilter();
            });
        });
        thread.setDaemon(true);
        thread.start();
    }

    private ObservableList<UnifiedRequest> fetchAllRequests() {
        ObservableList<UnifiedRequest> data = FXCollections.observableArrayList();
        fetchPasswordResetRequests(data);
        fetchPassSlipRequests(data);
        fetchSignatureRequests(data);
        data.sort((a, b) -> {
            if (a.getCreatedAt() == null && b.getCreatedAt() == null) return 0;
            if (a.getCreatedAt() == null) return 1;
            if (b.getCreatedAt() == null) return -1;
            return b.getCreatedAt().compareTo(a.getCreatedAt());
        });
        return data;
    }

    private void fetchPasswordResetRequests(ObservableList<UnifiedRequest> data) {
        try (Connection connection = DatabaseConnection.connect()) {
            PreparedStatement stmt = connection.prepareStatement(
                    "SELECT * FROM password_reset_requests WHERE status = 'PENDING' AND (used = FALSE OR used IS NULL) ORDER BY requested_at DESC"
            );
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                PasswordResetRequest prr = new PasswordResetRequest(
                        rs.getInt("id"),
                        rs.getString("email"),
                        rs.getTimestamp("requested_at"),
                        rs.getString("status"),
                        rs.getTimestamp("approved_at"),
                        rs.getBoolean("used")
                );
                data.add(new UnifiedRequest(
                        prr.getId(),
                        UnifiedRequest.Category.PASSWORD_RESET,
                        prr.getEmail(),
                        "Password reset requested",
                        prr.getStatus(),
                        prr.getRequestedAt(),
                        prr
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void fetchPassSlipRequests(ObservableList<UnifiedRequest> data) {
        try (Connection connection = DatabaseConnection.connect()) {
            PreparedStatement stmt = connection.prepareStatement("""
                    SELECT ps.id, ps.employee_id, e.first_name, e.last_name, e.department,
                           ps.reason, ps.time_out, ps.estimated_return, ps.status
                    FROM pass_slips ps
                    JOIN employees e ON ps.employee_id = e.id
                    WHERE ps.status = 'PENDING'
                    ORDER BY ps.time_out DESC
                    """);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                PassSlip ps = new PassSlip(
                        rs.getInt("id"),
                        rs.getInt("employee_id"),
                        rs.getString("first_name") + " " + rs.getString("last_name"),
                        rs.getString("department"),
                        rs.getString("reason"),
                        rs.getTimestamp("time_out") != null ? rs.getTimestamp("time_out").toLocalDateTime() : null,
                        null,
                        null,
                        rs.getTimestamp("estimated_return") != null ? rs.getTimestamp("estimated_return").toLocalDateTime() : null,
                        0,
                        rs.getString("status")
                );
                data.add(new UnifiedRequest(
                        ps.getId(),
                        UnifiedRequest.Category.PASS_SLIP,
                        ps.getEmployeeName(),
                        ps.getDepartment() != null ? ps.getDepartment() : "N/A",
                        ps.getStatus(),
                        rs.getTimestamp("time_out"),
                        ps
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void fetchSignatureRequests(ObservableList<UnifiedRequest> data) {
        try (Connection connection = DatabaseConnection.connect()) {
            PreparedStatement stmt = connection.prepareStatement("""
                    SELECT sr.id, sr.user_id, u.username AS staff_name, sr.signature_name,
                           sr.image_data, sr.status, sr.created_at
                    FROM signature_requests sr
                    JOIN users u ON sr.user_id = u.id
                    ORDER BY sr.created_at DESC
                    """);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                SignatureRequest sr = new SignatureRequest(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("staff_name"),
                        rs.getString("signature_name"),
                        rs.getBytes("image_data"),
                        rs.getString("status"),
                        rs.getTimestamp("created_at")
                );
                data.add(new UnifiedRequest(
                        sr.getId(),
                        UnifiedRequest.Category.SIGNATURE,
                        sr.getStaffName(),
                        sr.getSignatureName(),
                        sr.getStatus(),
                        sr.getCreatedAt(),
                        sr
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updatePendingCount() {
        if (allRequests == null) return;
        long count = allRequests.stream()
                .filter(r -> "PENDING".equals(r.getStatus()) || "OUT".equals(r.getStatus()))
                .count();
        lblTotalPending.setText(String.valueOf(count));
    }

    private void processSelected(String newStatus) {
        UnifiedRequest selected = requestsTable.getSelectionModel().getSelectedItem();
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

        if ("CANCELLED".equals(newStatus) && selected.getCategory() != UnifiedRequest.Category.PASS_SLIP) {
            messageLabel.setText("Cancel is only available for pass slip requests.");
            messageLabel.setStyle("-fx-text-fill: #FCA5A5; -fx-font-weight: bold;");
            return;
        }

        switch (selected.getCategory()) {
            case PASSWORD_RESET -> processPasswordReset(selected.getRawData(PasswordResetRequest.class), newStatus);
            case SIGNATURE -> processSignature(selected.getRawData(SignatureRequest.class), newStatus);
            case PASS_SLIP -> processPassSlip(selected.getRawData(PassSlip.class), newStatus);
        }
    }

    private void processPassSlip(PassSlip passSlip, String newStatus) {
        String dbStatus;
        if ("APPROVED".equals(newStatus)) {
            dbStatus = "OUT";
        } else if ("CANCELLED".equals(newStatus)) {
            dbStatus = "CANCELLED";
        } else {
            dbStatus = "REJECTED";
        }
        try (Connection connection = DatabaseConnection.connect()) {
            String sql = "UPDATE pass_slips SET status = ? WHERE id = ?";
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, dbStatus);
            stmt.setInt(2, passSlip.getId());
            int updated = stmt.executeUpdate();

            if (updated > 0) {
                if ("OUT".equals(dbStatus)) {
                    utils.TimerService.markOut(passSlip.getEmployeeId(), passSlip.getEmployeeName());
                }
                messageLabel.setText("Pass slip " + dbStatus.toLowerCase() + " successfully.");
                messageLabel.setStyle("-fx-text-fill: #34D399; -fx-font-weight: bold;");
                ActivityLogController.logActivity(
                        "Pass slip " + dbStatus.toLowerCase() + " for: " + passSlip.getEmployeeName(),
                        passSlip.getEmployeeId()
                );
                loadAllRequestsAsync();
            } else {
                messageLabel.setText("Failed to update pass slip.");
                messageLabel.setStyle("-fx-text-fill: #FCA5A5; -fx-font-weight: bold;");
            }
        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("Database error: " + e.getMessage());
            messageLabel.setStyle("-fx-text-fill: #FCA5A5; -fx-font-weight: bold;");
        }
    }

    private void processPasswordReset(PasswordResetRequest request, String newStatus) {
        try (Connection connection = DatabaseConnection.connect()) {
            String sql = "APPROVED".equals(newStatus)
                    ? "UPDATE password_reset_requests SET status = ?, approved_at = ? WHERE id = ?"
                    : "UPDATE password_reset_requests SET status = ? WHERE id = ?";
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, newStatus);
            if ("APPROVED".equals(newStatus)) {
                stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now(PhilTime.ZONE)));
                stmt.setInt(3, request.getId());
            } else {
                stmt.setInt(2, request.getId());
            }
            int updated = stmt.executeUpdate();
            if (updated > 0) {
                messageLabel.setText("Password reset " + newStatus.toLowerCase() + " successfully.");
                messageLabel.setStyle("-fx-text-fill: #34D399; -fx-font-weight: bold;");
                ActivityLogController.logActivity("Password reset request " + newStatus.toLowerCase() + " for: " + request.getEmail(), 0);
                loadAllRequestsAsync();
            } else {
                messageLabel.setText("Failed to update request.");
                messageLabel.setStyle("-fx-text-fill: #FCA5A5; -fx-font-weight: bold;");
            }
        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("Database error: " + e.getMessage());
            messageLabel.setStyle("-fx-text-fill: #FCA5A5; -fx-font-weight: bold;");
        }
    }

    private void processSignature(SignatureRequest request, String newStatus) {
        try (Connection connection = DatabaseConnection.connect()) {
            if ("APPROVED".equals(newStatus)) {
                PreparedStatement del = connection.prepareStatement("DELETE FROM signatures WHERE user_id = ?");
                del.setInt(1, request.getUserId());
                del.executeUpdate();
                del.close();

                PreparedStatement ins = connection.prepareStatement(
                        "INSERT INTO signatures (user_id, signature_name, image_data) VALUES (?, ?, ?)");
                ins.setInt(1, request.getUserId());
                ins.setString(2, request.getSignatureName());
                ins.setBytes(3, request.getImageData());
                ins.executeUpdate();
                ins.close();
            }

            PreparedStatement upd = connection.prepareStatement(
                    "UPDATE signature_requests SET status = ?, reviewed_by = ?, reviewed_at = ? WHERE id = ?");
            upd.setString(1, newStatus);
            upd.setInt(2, Session.currentUserId);
            upd.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now(PhilTime.ZONE)));
            upd.setInt(4, request.getId());
            int updated = upd.executeUpdate();

            if (updated > 0) {
                messageLabel.setText("Signature request " + newStatus.toLowerCase() + " successfully.");
                messageLabel.setStyle("-fx-text-fill: #34D399; -fx-font-weight: bold;");
                ActivityLogController.logActivity("Signature request " + newStatus.toLowerCase() + " for: " + request.getStaffName(), request.getUserId());
                loadAllRequestsAsync();
            } else {
                messageLabel.setText("Failed to update request.");
                messageLabel.setStyle("-fx-text-fill: #FCA5A5; -fx-font-weight: bold;");
            }
        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("Database error: " + e.getMessage());
            messageLabel.setStyle("-fx-text-fill: #FCA5A5; -fx-font-weight: bold;");
        }
    }
}
