package controllers;

import database.DatabaseConnection;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import utils.NavigationHelper;
import utils.PhilTime;
import utils.Session;
import utils.SidebarHelper;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EmployeeImportController {

    @FXML private Button btnSidebarDashboard;
    @FXML private Button btnSidebarMonitoring;
    @FXML private Button btnSidebarEmployees;
    @FXML private Button btnSidebarReports;
    @FXML private Button btnSidebarLogReturn;
    @FXML private Button btnSidebarUsers;
    @FXML private Button btnSidebarSignatures;
    @FXML private Button btnSidebarPasswordReset;
    @FXML private Button btnLogout;
    @FXML private Button btnNotificationsAlert;
    @FXML private Button btnChooseFile;
    @FXML private Button btnImportAll;
    @FXML private Button btnBackToEmployees;
    @FXML private Label lblMessage;
    @FXML private Label lblImportSummary;
    @FXML private TableView<ImportRow> previewTable;

    @FXML private TableColumn<ImportRow, Number> colPreviewRowNum;
    @FXML private TableColumn<ImportRow, String> colPreviewFirstName;
    @FXML private TableColumn<ImportRow, String> colPreviewLastName;
    @FXML private TableColumn<ImportRow, String> colPreviewDepartment;
    @FXML private TableColumn<ImportRow, String> colPreviewPosition;
    @FXML private TableColumn<ImportRow, String> colPreviewContact;
    @FXML private TableColumn<ImportRow, String> colPreviewJoinDate;
    @FXML private TableColumn<ImportRow, String> colPreviewManager;
    @FXML private TableColumn<ImportRow, String> colPreviewEmail;
    @FXML private TableColumn<ImportRow, String> colPreviewAddress;
    @FXML private TableColumn<ImportRow, String> colPreviewEmergency;

    @FXML private Button btnManageEmployees;
    @FXML private VBox manageEmployeesSubMenu;

    private List<ImportRow> importData = new ArrayList<>();

    @FXML
    private void initialize() {
        SidebarHelper.initialize(
                btnSidebarDashboard, btnSidebarMonitoring,
                btnSidebarEmployees, btnSidebarReports,
                btnSidebarLogReturn, btnSidebarUsers,
                btnSidebarSignatures, btnSidebarPasswordReset,
                btnLogout, btnNotificationsAlert,
                btnSidebarEmployees
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

        btnBackToEmployees.setOnAction(e -> NavigationHelper.navigateTo(btnBackToEmployees, "/fxml/EmployeeController.fxml"));
        btnChooseFile.setOnAction(e -> chooseFile());
        btnImportAll.setOnAction(e -> importAll());

        setupTableColumns();
    }

    private void setupTableColumns() {
        colPreviewRowNum.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleIntegerProperty(previewTable.getItems().indexOf(cellData.getValue()) + 1));
        colPreviewFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        colPreviewLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        colPreviewDepartment.setCellValueFactory(new PropertyValueFactory<>("department"));
        colPreviewPosition.setCellValueFactory(new PropertyValueFactory<>("position"));
        colPreviewContact.setCellValueFactory(new PropertyValueFactory<>("contact"));
        colPreviewJoinDate.setCellValueFactory(new PropertyValueFactory<>("joinDate"));
        colPreviewManager.setCellValueFactory(new PropertyValueFactory<>("manager"));
        colPreviewEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colPreviewAddress.setCellValueFactory(new PropertyValueFactory<>("address"));
        colPreviewEmergency.setCellValueFactory(new PropertyValueFactory<>("emergencyContact"));
    }

    private void chooseFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Employee Import File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx")
        );

        File file = fileChooser.showOpenDialog(btnChooseFile.getScene().getWindow());
        if (file == null) return;

        lblMessage.setText("Loading file...");
        lblMessage.setStyle("-fx-text-fill: #A8A29E; -fx-font-size: 13px;");

        Task<List<ImportRow>> loadTask = new Task<>() {
            @Override
            protected List<ImportRow> call() throws Exception {
                String name = file.getName().toLowerCase();
                if (name.endsWith(".xlsx")) {
                    return parseExcel(file);
                } else {
                    throw new IllegalArgumentException("Unsupported file format. Only .xlsx files are supported.");
                }
            }
        };

        loadTask.setOnSucceeded(event -> {
            importData = loadTask.getValue();
            validateData();
            previewTable.setItems(FXCollections.observableArrayList(importData));
            btnImportAll.setDisable(importData.isEmpty());
            lblImportSummary.setText(importData.size() + " records loaded");
            lblMessage.setText("File loaded successfully. Review preview below.");
            lblMessage.setStyle("-fx-text-fill: #34D399; -fx-font-size: 13px;");
        });

        loadTask.setOnFailed(event -> {
            lblMessage.setText("Error: " + loadTask.getException().getMessage());
            lblMessage.setStyle("-fx-text-fill: #FCA5A5; -fx-font-size: 13px;");
        });

        new Thread(loadTask).start();
    }

    private List<ImportRow> parseExcel(File file) throws Exception {
        List<ImportRow> rows = new ArrayList<>();
        try (Workbook workbook = new XSSFWorkbook(file)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                if (isRowEmpty(row)) continue;

                ImportRow importRow = new ImportRow();
                importRow.firstName = getCellStringValue(row.getCell(0));
                importRow.lastName = getCellStringValue(row.getCell(1));
                importRow.department = getCellStringValue(row.getCell(2));
                importRow.position = getCellStringValue(row.getCell(3));
                importRow.contact = getCellStringValue(row.getCell(4));
                importRow.joinDate = getCellDateValue(row.getCell(5));
                importRow.manager = getCellStringValue(row.getCell(6));
                importRow.email = getCellStringValue(row.getCell(7));
                importRow.address = getCellStringValue(row.getCell(8));
                importRow.emergencyContact = getCellStringValue(row.getCell(9));
                rows.add(importRow);
            }
        }
        return rows;
    }

    private String getCellStringValue(Cell cell) {
        if (cell == null) return "";
        cell.setCellType(CellType.STRING);
        return cell.getStringCellValue().trim();
    }

    private String getCellDateValue(Cell cell) {
        if (cell == null) return "";
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getDateCellValue().toInstant()
                    .atZone(PhilTime.ZONE)
                    .toLocalDate()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }
        return getCellStringValue(cell);
    }

    private boolean isRowEmpty(Row row) {
        for (int c = 0; c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK
                    && !getCellStringValue(cell).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private void validateData() {
        Set<String> existingEmails = loadExistingEmails();
        Set<String> emailsInFile = new HashSet<>();

        for (int i = 0; i < importData.size(); i++) {
            ImportRow row = importData.get(i);
            List<String> errors = new ArrayList<>();

            if (row.firstName.isEmpty()) errors.add("Missing first name");
            if (row.lastName.isEmpty()) errors.add("Missing last name");
            if (row.department.isEmpty()) errors.add("Missing department");
            if (row.position.isEmpty()) errors.add("Missing position");
            if (row.contact.isEmpty()) errors.add("Missing contact");
            else if (!row.contact.matches("[0-9]+")) errors.add("Contact must be numbers only");
            else if (row.contact.length() < 7) errors.add("Invalid contact number");

            if (row.joinDate.isEmpty()) errors.add("Missing join date");
            else {
                try {
                    LocalDate.parse(row.joinDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                } catch (DateTimeParseException e) {
                    errors.add("Invalid date format (use yyyy-MM-dd)");
                }
            }

            if (row.email.isEmpty()) errors.add("Missing email");
            else if (!row.email.endsWith("@pup.edu.ph")) errors.add("Email must end with @pup.edu.ph");
            else if (existingEmails.contains(row.email.toLowerCase())) errors.add("Duplicate employee email in database");
            else if (!emailsInFile.add(row.email.toLowerCase())) errors.add("Duplicate email in file");

            row.errors = errors;
            row.valid = errors.isEmpty();
        }
    }

    private Set<String> loadExistingEmails() {
        Set<String> emails = new HashSet<>();
        try {
            Connection conn = DatabaseConnection.connect();
            if (conn == null) return emails;
            PreparedStatement ps = conn.prepareStatement("SELECT LOWER(email) FROM employees WHERE email IS NOT NULL AND email != ''");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                emails.add(rs.getString(1));
            }
            rs.close();
            ps.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return emails;
    }

    private void importAll() {
        if (importData.isEmpty()) return;

        btnImportAll.setDisable(true);
        btnChooseFile.setDisable(true);

        Task<int[]> importTask = new Task<>() {
            @Override
            protected int[] call() {
                int imported = 0;
                int skipped = 0;
                int failed = 0;

                try {
                    Connection conn = DatabaseConnection.connect();
                    if (conn == null) return new int[]{0, 0, 0};

                    String sql = """
                            INSERT INTO employees
                            (first_name, last_name, department, position,
                             contact, join_date, manager, email, address, emergency_contact)
                            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                            """;
                    PreparedStatement ps = conn.prepareStatement(sql);

                    for (ImportRow row : importData) {
                        if (!row.valid) {
                            failed++;
                            continue;
                        }

                        try {
                            ps.setString(1, row.firstName);
                            ps.setString(2, row.lastName);
                            ps.setString(3, row.department);
                            ps.setString(4, row.position);
                            ps.setString(5, row.contact);
                            ps.setDate(6, java.sql.Date.valueOf(
                                    LocalDate.parse(row.joinDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"))));
                            ps.setString(7, row.manager != null ? row.manager : "");
                            ps.setString(8, row.email);
                            ps.setString(9, row.address != null ? row.address : "");
                            ps.setString(10, row.emergencyContact != null ? row.emergencyContact : "");
                            ps.executeUpdate();
                            imported++;
                        } catch (Exception e) {
                            failed++;
                        }
                    }

                    ps.close();
                    conn.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return new int[]{imported, skipped, failed};
            }
        };

        importTask.setOnSucceeded(event -> {
            int[] result = importTask.getValue();
            int imported = result[0];
            int skipped = result[1];
            int failed = result[2];

            lblImportSummary.setText(
                    "Imported: " + imported + " employees | Skipped: " + skipped + " | Failed: " + failed
            );

            if (imported > 0) {
                lblMessage.setText("Import completed successfully!");
                lblMessage.setStyle("-fx-text-fill: #34D399; -fx-font-size: 13px;");
                ActivityLogController.logActivity(
                        Session.currentUsername + " imported " + imported + " employees", 0);
            } else {
                lblMessage.setText("No employees were imported. Check validation errors.");
                lblMessage.setStyle("-fx-text-fill: #FCA5A5; -fx-font-size: 13px;");
            }

            btnImportAll.setDisable(false);
            btnChooseFile.setDisable(false);
        });

        importTask.setOnFailed(event -> {
            lblMessage.setText("Import failed: " + importTask.getException().getMessage());
            lblMessage.setStyle("-fx-text-fill: #FCA5A5; -fx-font-size: 13px;");
            btnImportAll.setDisable(false);
            btnChooseFile.setDisable(false);
        });

        new Thread(importTask).start();
    }

    public static class ImportRow {
        private String firstName = "";
        private String lastName = "";
        private String department = "";
        private String position = "";
        private String contact = "";
        private String joinDate = "";
        private String manager = "";
        private String email = "";
        private String address = "";
        private String emergencyContact = "";
        private List<String> errors = new ArrayList<>();
        private boolean valid = true;

        public String getFirstName() { return firstName; }
        public String getLastName() { return lastName; }
        public String getDepartment() { return department; }
        public String getPosition() { return position; }
        public String getContact() { return contact; }
        public String getJoinDate() { return joinDate; }
        public String getManager() { return manager; }
        public String getEmail() { return email; }
        public String getAddress() { return address; }
        public String getEmergencyContact() { return emergencyContact; }
        public List<String> getErrors() { return errors; }
        public boolean isValid() { return valid; }
    }
}
