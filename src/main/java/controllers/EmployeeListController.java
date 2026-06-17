package controllers;

import database.DatabaseConnection;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import models.Employee;
import utils.NavigationHelper;
import utils.Session;
import utils.ConfirmDialog;
import utils.SidebarHelper;

import java.io.File;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class EmployeeListController {

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
    private Button btnSidebarPasswordReset;

    @FXML
    private Button btnLogout;

    @FXML
    private Button btnNotificationsAlert;

    @FXML
    private VBox cardCreateEmployee;

    @FXML
    private Label lblTotalEmployees;

    @FXML
    private Label lblTotalDepartments;

    @FXML
    private TextField txtSearchEmployee;

    @FXML
    private Button btnFilterAll;

    @FXML
    private Button btnFilterEducation;

    @FXML
    private Button btnFilterIT;

    @FXML
    private Button btnFilterEngineering;

    @FXML
    private Button btnFilterHRM;

    @FXML
    private Button btnFilterAccountancy;

    @FXML
    private TableView<Employee> employeeTableView;

    @FXML
    private TableColumn<Employee, String> colEmployeeName;

    @FXML
    private TableColumn<Employee, String> colDepartment;

    @FXML
    private TableColumn<Employee, String> colPosition;

    @FXML
    private TableColumn<Employee, String> colAction;

    @FXML
    private CheckBox chkSelectAll;

    @FXML
    private Button btnBatchDelete;

    @FXML
    private Button btnExportCsv;

    @FXML
    private Button btnExportExcel;

    @FXML
    private TableColumn<Employee, Boolean> colSelect;

    @FXML
    private Button btnManageEmployees;

    @FXML
    private Button btnBackToEmployees;

    @FXML
    private VBox manageEmployeesSubMenu;

    private FilteredList<Employee> filteredEmployees;
    private ObservableList<Employee> selectedEmployees = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        SidebarHelper.initialize(
                btnSidebarDashboard, btnSidebarMonitoring,
                btnSidebarEmployeeDirectory, btnSidebarAddEmployee, btnSidebarImportEmployee,
                btnSidebarReports,
                btnSidebarLogReturn, btnSidebarUsers,
                btnSidebarSignatures, btnSidebarPasswordReset,
                btnLogout, btnNotificationsAlert,
                btnSidebarEmployeeDirectory
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

        if (btnBackToEmployees != null)
            btnBackToEmployees.setOnAction(e -> NavigationHelper.navigateTo(btnBackToEmployees, "/fxml/EmployeeController.fxml"));

        cardCreateEmployee.setOnMouseClicked(
                event -> {
                    Session.selectedEmployeeId = 0;
                    NavigationHelper.navigateTo(
                            cardCreateEmployee,
                            "/fxml/EmployeeForm.fxml"
                    );
                }
        );

        if (btnExportCsv != null)
            btnExportCsv.setOnAction(event -> exportCsv());
        if (btnExportExcel != null)
            btnExportExcel.setOnAction(event -> exportExcel());

        colEmployeeName.setCellValueFactory(
                cellData -> new ReadOnlyStringWrapper(
                        cellData.getValue().getFirstName()
                                + " "
                                + cellData.getValue().getLastName()
                )
        );

        colDepartment.setCellValueFactory(
                cellData -> new ReadOnlyStringWrapper(
                        cellData.getValue().getDepartment()
                )
        );

        colPosition.setCellValueFactory(
                cellData -> new ReadOnlyStringWrapper(
                        cellData.getValue().getPosition()
                )
        );

        colAction.setCellValueFactory(
                cellData -> new ReadOnlyStringWrapper("View Profile")
        );

        colSelect.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleBooleanProperty(false)
        );

        colSelect.setCellFactory(column -> new TableCell<>() {
            private final CheckBox checkBox = new CheckBox();

            {
                checkBox.setOnAction(event -> {
                    Employee employee = getTableView().getItems().get(getIndex());
                    if (checkBox.isSelected()) {
                        selectedEmployees.add(employee);
                    } else {
                        selectedEmployees.remove(employee);
                    }
                    updateDeleteButton();
                });
            }

            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                } else {
                    Employee employee = getTableView().getItems().get(getIndex());
                    checkBox.setSelected(selectedEmployees.contains(employee));
                    setGraphic(checkBox);
                }
            }
        });

        chkSelectAll.setOnAction(event -> {
            if (chkSelectAll.isSelected()) {
                selectedEmployees.addAll(filteredEmployees);
            } else {
                selectedEmployees.clear();
            }
            employeeTableView.refresh();
            updateDeleteButton();
        });

        btnBatchDelete.setOnAction(event -> batchDeleteEmployees());

        employeeTableView.setOnMouseClicked(
                event -> {
                    Employee selectedEmployee =
                            employeeTableView.getSelectionModel()
                                    .getSelectedItem();

                    if (selectedEmployee != null
                            && event.getClickCount() >= 2) {
                        Session.selectedEmployeeId =
                                selectedEmployee.getId();

                        NavigationHelper.navigateTo(
                                employeeTableView,
                                "/fxml/EmployeeDetails.fxml"
                        );
                    }
                }
        );

        txtSearchEmployee.textProperty().addListener(
                (observable, oldValue, newValue) ->
                        applyFilter(newValue, null)
        );

        btnFilterAll.setOnAction(
                event -> applyFilter(txtSearchEmployee.getText(), null)
        );

        btnFilterEducation.setOnAction(
                event -> applyFilter(txtSearchEmployee.getText(), "Education")
        );

        btnFilterIT.setOnAction(
                event -> applyFilter(txtSearchEmployee.getText(), "IT")
        );

        btnFilterEngineering.setOnAction(
                event -> applyFilter(txtSearchEmployee.getText(), "Engineering")
        );

        btnFilterHRM.setOnAction(
                event -> applyFilter(txtSearchEmployee.getText(), "HRM")
        );

        btnFilterAccountancy.setOnAction(
                event -> applyFilter(txtSearchEmployee.getText(), "Accountancy")
        );

        loadEmployeesAsync();

    }

    private void loadEmployeesAsync() {

        Task<ObservableList<Employee>> task = new Task<>() {
            @Override
            protected ObservableList<Employee> call() {
                return getEmployees();
            }
        };

        task.setOnSucceeded(e -> {
            ObservableList<Employee> employees = task.getValue();
            filteredEmployees = new FilteredList<>(employees, employee -> true);
            employeeTableView.setItems(filteredEmployees);

            lblTotalEmployees.setText(String.valueOf(employees.size()));

            Set<String> departments = employees.stream()
                    .map(Employee::getDepartment)
                    .filter(value -> value != null && !value.isBlank())
                    .collect(Collectors.toSet());

            lblTotalDepartments.setText(String.valueOf(departments.size()));
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();

    }

    public static ObservableList<Employee> getEmployees() {

        ObservableList<Employee> employeeList =
                FXCollections.observableArrayList();

        try (Connection connection = DatabaseConnection.connect()) {

            String query =
                    "SELECT * FROM employees ORDER BY id ASC";

            PreparedStatement statement =
                    connection.prepareStatement(query);

            ResultSet resultSet =
                    statement.executeQuery();

            while(resultSet.next()) {

                Employee employee = new Employee(
                        resultSet.getInt("id"),
                        resultSet.getString("first_name"),
                        resultSet.getString("last_name"),
                        resultSet.getString("department"),
                        resultSet.getString("position"),
                        resultSet.getString("contact"),
                        resultSet.getDate("join_date").toLocalDate(),
                        resultSet.getString("manager"),
                        resultSet.getString("email"),
                        resultSet.getString("address"),
                        resultSet.getString("emergency_contact")
                );

                employeeList.add(employee);

            }

        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return employeeList;

    }

    private void applyFilter(String keyword, String department) {

        if (filteredEmployees == null) return;

        filteredEmployees.setPredicate(employee -> {

            boolean matchesKeyword =
                    keyword == null
                            || keyword.isBlank()
                            || (employee.getFirstName()
                            + " "
                            + employee.getLastName())
                            .toLowerCase()
                            .contains(keyword.toLowerCase())
                            || String.valueOf(employee.getId())
                            .contains(keyword);

            boolean matchesDepartment =
                    department == null
                            || department.equalsIgnoreCase(
                            employee.getDepartment()
                    );

            return matchesKeyword && matchesDepartment;

        });

    }

    private void updateDeleteButton() {
        if (selectedEmployees.isEmpty()) {
            btnBatchDelete.setVisible(false);
            btnBatchDelete.setManaged(false);
        } else {
            btnBatchDelete.setVisible(true);
            btnBatchDelete.setManaged(true);
            btnBatchDelete.setText("\uD83D\uDDD1 Delete Selected (" + selectedEmployees.size() + ")");
        }
    }

    private void batchDeleteEmployees() {
        if (selectedEmployees.isEmpty()) return;

        boolean confirmed = ConfirmDialog.show(
                employeeTableView,
                "Delete Employees",
                "Are you sure you want to delete " + selectedEmployees.size() + " employee(s)?\nThis will also remove all their pass slips and activity logs."
        );

        if (!confirmed) return;

        int count = selectedEmployees.size();

        try (Connection connection = DatabaseConnection.connect()) {

            for (Employee employee : selectedEmployees) {
                int employeeId = employee.getId();

                PreparedStatement logsStmt = connection.prepareStatement(
                        "DELETE FROM activity_logs WHERE employee_id = ?"
                );
                logsStmt.setInt(1, employeeId);
                logsStmt.executeUpdate();
                logsStmt.close();

                PreparedStatement slipsStmt = connection.prepareStatement(
                        "DELETE FROM pass_slips WHERE employee_id = ?"
                );
                slipsStmt.setInt(1, employeeId);
                slipsStmt.executeUpdate();
                slipsStmt.close();

                PreparedStatement empStmt = connection.prepareStatement(
                        "DELETE FROM employees WHERE id = ?"
                );
                empStmt.setInt(1, employeeId);
                empStmt.executeUpdate();
                empStmt.close();
            }

            ActivityLogController.logActivity(
                    "Batch deleted " + count + " employee(s)", 0
            );

            selectedEmployees.clear();
            chkSelectAll.setSelected(false);
            updateDeleteButton();
            loadEmployeesAsync();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void exportCsv() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Employees as CSV");
        fileChooser.setInitialFileName("employees.csv");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );

        File file = fileChooser.showSaveDialog(btnExportCsv.getScene().getWindow());
        if (file == null) return;

        try (PrintWriter writer = new PrintWriter(file)) {
            writer.println("ID,First Name,Last Name,Department,Position,Contact,Join Date,Manager,Email,Address,Emergency Contact");

            for (Employee emp : filteredEmployees) {
                writer.println(
                        emp.getId() + ","
                                + "\"" + safe(emp.getFirstName()) + "\","
                                + "\"" + safe(emp.getLastName()) + "\","
                                + "\"" + safe(emp.getDepartment()) + "\","
                                + "\"" + safe(emp.getPosition()) + "\","
                                + "\"" + safe(emp.getContact()) + "\","
                                + (emp.getJoinDate() != null ? emp.getJoinDate() : "") + ","
                                + "\"" + safe(emp.getManager()) + "\","
                                + "\"" + safe(emp.getEmail()) + "\","
                                + "\"" + safe(emp.getAddress()) + "\","
                                + "\"" + safe(emp.getEmergencyContact()) + "\""
                );
            }

            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
            alert.setTitle("Export Successful");
            alert.setHeaderText(null);
            alert.setContentText("CSV exported to:\n" + file.getAbsolutePath());
            alert.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Export Failed");
            alert.setHeaderText(null);
            alert.setContentText("Failed to export CSV.");
            alert.showAndWait();
        }
    }

    private void exportExcel() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Employees as Excel");
        fileChooser.setInitialFileName("employees.xlsx");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx")
        );

        File file = fileChooser.showSaveDialog(btnExportExcel.getScene().getWindow());
        if (file == null) return;

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Employees");

            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "First Name", "Last Name", "Department", "Position", "Contact", "Join Date", "Manager", "Email", "Address", "Emergency Contact"};
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (Employee emp : filteredEmployees) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(emp.getId());
                row.createCell(1).setCellValue(emp.getFirstName() != null ? emp.getFirstName() : "");
                row.createCell(2).setCellValue(emp.getLastName() != null ? emp.getLastName() : "");
                row.createCell(3).setCellValue(emp.getDepartment() != null ? emp.getDepartment() : "");
                row.createCell(4).setCellValue(emp.getPosition() != null ? emp.getPosition() : "");
                row.createCell(5).setCellValue(emp.getContact() != null ? emp.getContact() : "");
                row.createCell(6).setCellValue(emp.getJoinDate() != null ? emp.getJoinDate().toString() : "");
                row.createCell(7).setCellValue(emp.getManager() != null ? emp.getManager() : "");
                row.createCell(8).setCellValue(emp.getEmail() != null ? emp.getEmail() : "");
                row.createCell(9).setCellValue(emp.getAddress() != null ? emp.getAddress() : "");
                row.createCell(10).setCellValue(emp.getEmergencyContact() != null ? emp.getEmergencyContact() : "");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            java.io.FileOutputStream fileOut = new java.io.FileOutputStream(file);
            workbook.write(fileOut);
            fileOut.close();

            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
            alert.setTitle("Export Successful");
            alert.setHeaderText(null);
            alert.setContentText("Excel exported to:\n" + file.getAbsolutePath());
            alert.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Export Failed");
            alert.setHeaderText(null);
            alert.setContentText("Failed to export Excel.");
            alert.showAndWait();
        }
    }

    private String safe(String s) {
        return s != null ? s.replace("\"", "\"\"") : "";
    }

}
