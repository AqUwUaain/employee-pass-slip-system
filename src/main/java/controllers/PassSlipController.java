package controllers;

import database.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import utils.Session;
import javafx.scene.layout.VBox;
import utils.NavigationHelper;
import utils.PhilTime;
import utils.TimerService;
import utils.ActivityLogger;

import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.itextpdf.io.image.ImageDataFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class PassSlipController {

    @FXML
    private Button btnTopClose;

    @FXML
    private Button btnCancelSlipAction;

    @FXML
    private Button btnSaveSlipAction;

    @FXML
    private TextField txtSlipYear;

    @FXML
    private TextField txtSlipFormattedDate;

    @FXML
    private TextField txtSlipClientId;

    @FXML
    private TextField txtSlipEmployeeName;

    @FXML
    private TextArea txtSlipPurpose;

    @FXML
    private TextField txtSlipTimeOut;

    @FXML
    private ComboBox<String> cmbTimeIn;

    @FXML
    private Label lblDuration;

    @FXML
    private ImageView imgAdminSignature;

    @FXML
    private Label lblSlipStatusMessage;

    @FXML
    private ComboBox<String> cmbEmployeeSelect;

    @FXML
    private VBox passSlipModalRoot;

    private int selectedEmployeeId = 0;

    private LocalDateTime selectedEstimatedReturn = null;

    @FXML
    private void initialize() {

        LocalDateTime now = LocalDateTime.now(PhilTime.ZONE);

        txtSlipYear.setText(String.valueOf(now.getYear()));
        txtSlipFormattedDate.setText(
                now.format(
                        DateTimeFormatter.ofPattern("MMMM dd, yyyy")
                ).toUpperCase()
        );
        txtSlipTimeOut.setText(
                now.format(
                        DateTimeFormatter.ofPattern("hh:mm a")
                )
        );

        loadEmployeeComboBox();
        populateTimeInOptions(now);
        loadAdminSignature();

        cmbTimeIn.setOnAction(event -> {
            handleTimeInSelection(now);
        });

        cmbTimeIn.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isBlank()) {
                handleTimeInManualInput(newValue.trim(), now);
            }
        });

        cmbTimeIn.getEditor().focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused && selectedEstimatedReturn != null) {
                String current = cmbTimeIn.getEditor().getText();
                if (current == null || current.isBlank()) {
                    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("hh:mm a");
                    cmbTimeIn.getEditor().setText(selectedEstimatedReturn.format(fmt));
                }
            }
        });

        cmbEmployeeSelect.setOnAction(event -> {
            String selected = cmbEmployeeSelect.getValue();
            if (selected != null && !selected.isEmpty()) {
                String[] parts = selected.split(" \\| ");
                if (parts.length >= 1) {
                    try {
                        selectedEmployeeId = Integer.parseInt(parts[0].trim());
                        txtSlipClientId.setText(String.valueOf(selectedEmployeeId));
                        loadEmployeeName(selectedEmployeeId);
                    } catch (NumberFormatException e) {
                        selectedEmployeeId = 0;
                    }
                }
            }
        });

        btnTopClose.setOnAction(
                event -> NavigationHelper.navigateToDashboard(btnTopClose)
        );

        btnCancelSlipAction.setOnAction(
                event -> NavigationHelper.navigateToDashboard(btnCancelSlipAction)
        );

        btnSaveSlipAction.setOnAction(event -> {
            issuePassSlip(
                    txtSlipClientId.getText(),
                    txtSlipPurpose.getText(),
                    lblSlipStatusMessage,
                    selectedEstimatedReturn
            );
            if ("PASS SLIP ISSUED".equals(lblSlipStatusMessage.getText())) {
                autoPrintSlip();
            }
        });

    }

    private void loadEmployeeComboBox() {

        ObservableList<String> employeeOptions =
                FXCollections.observableArrayList();

        try (Connection connection = DatabaseConnection.connect()) {

            String query = "SELECT id, first_name, last_name FROM employees ORDER BY id";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("first_name")
                        + " " + resultSet.getString("last_name");
                employeeOptions.add(id + " | " + name);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        cmbEmployeeSelect.setItems(employeeOptions);

    }

    private void populateTimeInOptions(LocalDateTime timeOut) {
        ObservableList<String> timeOptions = FXCollections.observableArrayList();
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");

        LocalDateTime startTime;
        int minuteOfHour = timeOut.getMinute();
        if (minuteOfHour <= 30) {
            startTime = timeOut.withMinute(30).withSecond(0).withNano(0);
        } else {
            startTime = timeOut.plusHours(1).withMinute(0).withSecond(0).withNano(0);
        }

        for (int i = 0; i < 16; i++) {
            LocalDateTime estimatedReturn = startTime.plusMinutes(i * 30L);
            long totalMinutes = java.time.Duration.between(timeOut, estimatedReturn).toMinutes();
            long hours = totalMinutes / 60;
            long mins = totalMinutes % 60;

            String duration;
            if (hours > 0 && mins > 0) {
                duration = hours + " hr" + (hours > 1 ? "s" : "") + " " + mins + " min";
            } else if (hours > 0) {
                duration = hours + " hr" + (hours > 1 ? "s" : "");
            } else {
                duration = mins + " min";
            }

            String timeStr = estimatedReturn.format(timeFormatter);
            timeOptions.add(timeStr + "  (" + duration + ")");
        }

        cmbTimeIn.setItems(timeOptions);
    }

    private void handleTimeInSelection(LocalDateTime now) {
        String selected = cmbTimeIn.getValue();
        if (selected != null) {
            String timeStr = selected.split("  \\(")[0].trim().toUpperCase();
            try {
                DateTimeFormatter parser = DateTimeFormatter.ofPattern("hh:mm a");
                LocalTime timeInParsed = LocalTime.parse(timeStr, parser);
                LocalDateTime timeIn = now.toLocalDate().atTime(timeInParsed);
                if (timeIn.isBefore(now)) {
                    timeIn = timeIn.plusDays(1);
                }
                selectedEstimatedReturn = timeIn;
                updateDurationLabel(now, timeIn);
                lblSlipStatusMessage.setText("");
            } catch (Exception e) {
                selectedEstimatedReturn = null;
                lblDuration.setText("");
            }
        }
    }

    private void handleTimeInManualInput(String input, LocalDateTime now) {
        if (input.isBlank()) {
            if (selectedEstimatedReturn == null) {
                lblDuration.setText("");
            }
            return;
        }

        try {
            DateTimeFormatter parser = DateTimeFormatter.ofPattern("hh:mm a");
            LocalTime timeInParsed = LocalTime.parse(input.toUpperCase(), parser);
            LocalDateTime timeIn = now.toLocalDate().atTime(timeInParsed);
            if (timeIn.isBefore(now)) {
                timeIn = timeIn.plusDays(1);
            }
            selectedEstimatedReturn = timeIn;
            updateDurationLabel(now, timeIn);
            lblSlipStatusMessage.setText("");
        } catch (Exception e) {
            selectedEstimatedReturn = null;
            lblDuration.setText("");
            lblSlipStatusMessage.setText("Invalid time format. Use HH:MM AM/PM (e.g., 09:30 AM)");
            lblSlipStatusMessage.setStyle("-fx-text-fill: #FCA5A5; -fx-font-size: 12px;");
        }
    }

    private void updateDurationLabel(LocalDateTime now, LocalDateTime timeIn) {
        long totalMinutes = java.time.Duration.between(now, timeIn).toMinutes();
        long hours = totalMinutes / 60;
        long mins = totalMinutes % 60;
        String durationText;
        if (hours > 0 && mins > 0) {
            durationText = "Estimated duration: " + hours + " hr" + (hours > 1 ? "s" : "") + " " + mins + " min" + (mins > 1 ? "s" : "");
        } else if (hours > 0) {
            durationText = "Estimated duration: " + hours + " hr" + (hours > 1 ? "s" : "");
        } else {
            durationText = "Estimated duration: " + mins + " min";
        }
        lblDuration.setText(durationText);
    }

    private void loadEmployeeName(int employeeId) {

        try (Connection connection = DatabaseConnection.connect()) {

            String query = "SELECT first_name, last_name FROM employees WHERE id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, employeeId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                txtSlipEmployeeName.setText(
                        resultSet.getString("first_name")
                                + " " + resultSet.getString("last_name")
                );
            } else {
                txtSlipEmployeeName.clear();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void loadAdminSignature() {
        byte[] imageData = SignatureController.getSignatureByUserId(Session.currentUserId);
        if (imageData != null) {
            ByteArrayInputStream bis = new ByteArrayInputStream(imageData);
            Image fxImage = new Image(bis);
            imgAdminSignature.setImage(fxImage);
        }
    }

    public static void issuePassSlip(

            String employeeId,
            String reason,
            Label messageLabel,
            LocalDateTime estimatedReturn

    ) {

        if (employeeId == null || employeeId.isEmpty()) {
            messageLabel.setText("SELECT EMPLOYEE");
            return;
        }

        if (reason == null || reason.trim().isEmpty()) {
            messageLabel.setText("REASON REQUIRED");
            return;
        }

        if (reason.trim().length() < 5) {
            messageLabel.setText("REASON TOO SHORT");
            return;
        }

        if (estimatedReturn == null) {
            messageLabel.setText("SELECT OR ENTER ESTIMATED RETURN TIME");
            return;
        }

        try (Connection connection = DatabaseConnection.connect()) {

            String checkQuery = "SELECT COUNT(*) FROM pass_slips WHERE employee_id = ? AND status = 'OUT'";
            PreparedStatement checkStmt = connection.prepareStatement(checkQuery);
            checkStmt.setInt(1, Integer.parseInt(employeeId));
            ResultSet checkRs = checkStmt.executeQuery();
            if (checkRs.next() && checkRs.getInt(1) > 0) {
                javafx.application.Platform.runLater(() -> {
                    javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
                    alert.setTitle("Employee Currently Out");
                    alert.setHeaderText(null);
                    alert.setContentText("This employee is already out on a pass slip.\nPlease wait for the employee to return before issuing a new slip.");
                    alert.showAndWait();
                });
                messageLabel.setText("EMPLOYEE IS CURRENTLY OUT");
                return;
            }

            String query = """
                    INSERT INTO pass_slips
                    (employee_id, reason, time_out, estimated_return, status)
                    VALUES (?, ?, ?, ?, ?)
                    """;

            PreparedStatement statement =
                    connection.prepareStatement(query);

            statement.setInt(1, Integer.parseInt(employeeId));
            statement.setString(2, reason.trim());
            statement.setTimestamp(3, java.sql.Timestamp.valueOf(LocalDateTime.now(PhilTime.ZONE)));
            if (estimatedReturn != null) {
                statement.setTimestamp(4, java.sql.Timestamp.valueOf(estimatedReturn));
            } else {
                statement.setNull(4, java.sql.Types.TIMESTAMP);
            }
            statement.setString(5, "OUT");

            int inserted = statement.executeUpdate();

            if (inserted > 0) {

                // Track in timer service for admin live timer
                String empName = getEmployeeNameById(
                        Integer.parseInt(employeeId),
                        connection
                );
                TimerService.markOut(Integer.parseInt(employeeId), empName);

                ActivityLogController.logActivity(
                        "Issued Pass Slip for Employee ID: " + employeeId,
                        Integer.parseInt(employeeId)
                );

                messageLabel.setText("PASS SLIP ISSUED");

            } else {
                messageLabel.setText("FAILED TO ISSUE PASS SLIP");
            }

        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("DATABASE ERROR");
        }

    }

    private static String getEmployeeNameById(int id, Connection connection) {
        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "SELECT first_name, last_name FROM employees WHERE id = ?"
            );
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("first_name") + " " + rs.getString("last_name");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Unknown";
    }

    private void autoPrintSlip() {
        if (selectedEmployeeId == 0) return;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Pass Slip as PDF");
        fileChooser.setInitialFileName("PassSlip_" + txtSlipClientId.getText() + ".pdf");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
        );

        File file = fileChooser.showSaveDialog(
                passSlipModalRoot.getScene().getWindow()
        );

        if (file != null) {
            try {
                generatePdf(file);
                lblSlipStatusMessage.setText("PASS SLIP ISSUED — PDF SAVED");

                String empName = txtSlipEmployeeName.getText();
                String purpose = txtSlipPurpose.getText();
                ActivityLogger.log(
                        "PRINT_SLIP",
                        "Printed pass slip for " + empName + " — Purpose: " + purpose,
                        selectedEmployeeId
                );
            } catch (Exception e) {
                e.printStackTrace();
                lblSlipStatusMessage.setText("FAILED TO SAVE PDF");
            }
        } else {
            lblSlipStatusMessage.setText("PASS SLIP ISSUED — PDF SAVE CANCELLED");
        }
    }

    private void generatePdf(File file) throws Exception {
        DeviceRgb white = new DeviceRgb(255, 255, 255);
        DeviceRgb gold = new DeviceRgb(212, 168, 83);
        DeviceRgb maroon = new DeviceRgb(124, 10, 2);
        DeviceRgb lightGray = new DeviceRgb(240, 240, 240);
        DeviceRgb medGray = new DeviceRgb(100, 100, 100);
        DeviceRgb darkText = new DeviceRgb(30, 30, 30);

        PdfWriter writer = new PdfWriter(file);
        PdfDocument pdfDoc = new PdfDocument(writer);
        com.itextpdf.kernel.geom.PageSize customSize = new com.itextpdf.kernel.geom.PageSize(595, 265);
        pdfDoc.addNewPage(customSize);
        Document document = new Document(pdfDoc, customSize);
        document.setMargins(5, 5, 5, 5);
        document.setBackgroundColor(white);

        Table headerTable = new Table(UnitValue.createPercentArray(new float[]{12, 88}))
                .useAllAvailableWidth()
                .setAutoLayout();

        Cell sideCell = new Cell(1, 1);
        sideCell.setBackgroundColor(maroon);
        sideCell.setVerticalAlignment(VerticalAlignment.MIDDLE);
        sideCell.setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.CENTER);
        sideCell.setKeepTogether(false);
        sideCell.setWidth(UnitValue.createPercentValue(12));
        Paragraph sideText = new Paragraph("PASS SLIP")
                .setFontColor(white)
                .setFontSize(16)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setRotationAngle(Math.toRadians(90));
        sideCell.add(sideText);
        headerTable.addCell(sideCell);

        Cell mainCell = new Cell(1, 1);
        mainCell.setPadding(8);
        mainCell.setBorder(new SolidBorder(medGray, 1));

        Table infoRow = new Table(UnitValue.createPercentArray(new float[]{20, 30, 25, 25}))
                .useAllAvailableWidth()
                .setAutoLayout();
        infoRow.addCell(createLabelCell("YEAR", medGray, white));
        infoRow.addCell(createValueCell(txtSlipYear.getText(), darkText, lightGray));
        infoRow.addCell(createLabelCell("EFFECTIVE SYSTEM LOG DATE", medGray, white));
        infoRow.addCell(createValueCell(txtSlipFormattedDate.getText(), gold, lightGray));
        mainCell.add(infoRow);

        Table idRow = new Table(UnitValue.createPercentArray(new float[]{20, 80}))
                .useAllAvailableWidth()
                .setAutoLayout();
        idRow.addCell(createLabelCell("CLIENT #:", medGray, white));
        idRow.addCell(createValueCell(txtSlipClientId.getText(), darkText, lightGray));
        mainCell.add(idRow);

        Table nameRow = new Table(UnitValue.createPercentArray(new float[]{20, 80}))
                .useAllAvailableWidth()
                .setAutoLayout();
        nameRow.addCell(createLabelCell("NAME:", medGray, white));
        nameRow.addCell(createValueCell(txtSlipEmployeeName.getText(), darkText, lightGray));
        mainCell.add(nameRow);

        Table purposeRow = new Table(UnitValue.createPercentArray(new float[]{20, 80}))
                .useAllAvailableWidth()
                .setAutoLayout();
        purposeRow.addCell(createLabelCell("PURPOSE:", medGray, white));
        purposeRow.addCell(createValueCell(txtSlipPurpose.getText(), darkText, lightGray));
        mainCell.add(purposeRow);

        Table timeRow = new Table(UnitValue.createPercentArray(new float[]{20, 30, 25, 25}))
                .useAllAvailableWidth()
                .setAutoLayout();
        timeRow.addCell(createLabelCell("TIME OUT:", medGray, white));
        timeRow.addCell(createValueCell(txtSlipTimeOut.getText(), darkText, lightGray));
        timeRow.addCell(createLabelCell("TIME IN:", medGray, white));
        String expectedTimeIn = selectedEstimatedReturn != null
                ? selectedEstimatedReturn.format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a"))
                : "Pending";
        timeRow.addCell(createValueCell(expectedTimeIn, darkText, lightGray));
        mainCell.add(timeRow);

        Table signeeRow = new Table(UnitValue.createPercentArray(new float[]{20, 80}))
                .useAllAvailableWidth()
                .setAutoLayout();
        signeeRow.addCell(createLabelCell("ADMIN SIGNATURE:", medGray, white));
        Cell signeeValueCell = new Cell();
        signeeValueCell.setBorder(new SolidBorder(new DeviceRgb(200, 200, 200), 0.5f));
        signeeValueCell.setPadding(5);
        signeeValueCell.setBackgroundColor(lightGray);
        byte[] sigData = SignatureController.getSignatureByUserId(Session.currentUserId);
        if (sigData != null) {
            com.itextpdf.layout.element.Image sigImage = new com.itextpdf.layout.element.Image(
                    ImageDataFactory.create(sigData)
            );
            sigImage.setWidth(150);
            sigImage.setHeight(50);
            signeeValueCell.add(sigImage);
        } else {
            signeeValueCell.add(new Paragraph(Session.currentUsername != null ? Session.currentUsername : "")
                    .setFontColor(gold)
                    .setFontSize(10));
        }
        signeeRow.addCell(signeeValueCell);
        mainCell.add(signeeRow);

        headerTable.addCell(mainCell);
        document.add(headerTable);
        document.close();
    }

    private void printSlip() {
        if (selectedEmployeeId == 0) {
            lblSlipStatusMessage.setText("SELECT AN EMPLOYEE BEFORE EXPORTING");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Pass Slip as PDF");
        fileChooser.setInitialFileName("PassSlip_" + txtSlipClientId.getText() + ".pdf");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
        );

        File file = fileChooser.showSaveDialog(
                passSlipModalRoot.getScene().getWindow()
        );

        if (file != null) {
            try {
                DeviceRgb white = new DeviceRgb(255, 255, 255);
                DeviceRgb black = new DeviceRgb(0, 0, 0);
                DeviceRgb gold = new DeviceRgb(212, 168, 83);
                DeviceRgb maroon = new DeviceRgb(124, 10, 2);
                DeviceRgb lightGray = new DeviceRgb(240, 240, 240);
                DeviceRgb medGray = new DeviceRgb(100, 100, 100);
                DeviceRgb darkText = new DeviceRgb(30, 30, 30);

                PdfWriter writer = new PdfWriter(file);
                PdfDocument pdfDoc = new PdfDocument(writer);
                com.itextpdf.kernel.geom.PageSize customSize = new com.itextpdf.kernel.geom.PageSize(595, 450);
                pdfDoc.addNewPage(customSize);
                Document document = new Document(pdfDoc, customSize);
                document.setMargins(15, 15, 15, 15);

                document.setBackgroundColor(white);

                Table headerTable = new Table(UnitValue.createPercentArray(new float[]{12, 88}))
                        .useAllAvailableWidth()
                        .setAutoLayout();

                Cell sideCell = new Cell(1, 1);
                sideCell.setBackgroundColor(maroon);
                sideCell.setVerticalAlignment(VerticalAlignment.MIDDLE);
                sideCell.setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.CENTER);
                sideCell.setKeepTogether(false);
                sideCell.setWidth(UnitValue.createPercentValue(12));
                Paragraph sideText = new Paragraph("PASS SLIP")
                        .setFontColor(white)
                        .setFontSize(16)
                        .setBold()
                        .setTextAlignment(TextAlignment.CENTER)
                        .setRotationAngle(Math.toRadians(90));
                sideCell.add(sideText);
                headerTable.addCell(sideCell);

                Cell mainCell = new Cell(1, 1);
        mainCell.setPadding(5);
                mainCell.setBorder(new SolidBorder(medGray, 1));

                Table infoRow = new Table(UnitValue.createPercentArray(new float[]{20, 30, 25, 25}))
                        .useAllAvailableWidth()
                        .setAutoLayout();
                infoRow.addCell(createLabelCell("YEAR", medGray, white));
                infoRow.addCell(createValueCell(txtSlipYear.getText(), darkText, lightGray));
                infoRow.addCell(createLabelCell("EFFECTIVE SYSTEM LOG DATE", medGray, white));
                infoRow.addCell(createValueCell(txtSlipFormattedDate.getText(), gold, lightGray));
                mainCell.add(infoRow);

                Table idRow = new Table(UnitValue.createPercentArray(new float[]{20, 80}))
                        .useAllAvailableWidth()
                        .setAutoLayout();
                idRow.addCell(createLabelCell("CLIENT #:", medGray, white));
                idRow.addCell(createValueCell(txtSlipClientId.getText(), darkText, lightGray));
                mainCell.add(idRow);

                Table nameRow = new Table(UnitValue.createPercentArray(new float[]{20, 80}))
                        .useAllAvailableWidth()
                        .setAutoLayout();
                nameRow.addCell(createLabelCell("NAME:", medGray, white));
                nameRow.addCell(createValueCell(txtSlipEmployeeName.getText(), darkText, lightGray));
                mainCell.add(nameRow);

                Table purposeRow = new Table(UnitValue.createPercentArray(new float[]{20, 80}))
                        .useAllAvailableWidth()
                        .setAutoLayout();
                purposeRow.addCell(createLabelCell("PURPOSE:", medGray, white));
                purposeRow.addCell(createValueCell(txtSlipPurpose.getText(), darkText, lightGray));
                mainCell.add(purposeRow);

                Table timeRow = new Table(UnitValue.createPercentArray(new float[]{20, 30, 25, 25}))
                        .useAllAvailableWidth()
                        .setAutoLayout();
                timeRow.addCell(createLabelCell("TIME OUT:", medGray, white));
                timeRow.addCell(createValueCell(txtSlipTimeOut.getText(), darkText, lightGray));
                timeRow.addCell(createLabelCell("TIME IN:", medGray, white));
                String timeInText = cmbTimeIn.getValue() != null
                        ? cmbTimeIn.getValue().split("  \\(")[0]
                        : (cmbTimeIn.getEditor().getText() != null && !cmbTimeIn.getEditor().getText().isBlank()
                                ? cmbTimeIn.getEditor().getText() : "Pending");
                timeRow.addCell(createValueCell(timeInText, darkText, lightGray));
                mainCell.add(timeRow);

                Table signeeRow = new Table(UnitValue.createPercentArray(new float[]{20, 80}))
                        .useAllAvailableWidth()
                        .setAutoLayout();
                signeeRow.addCell(createLabelCell("ADMIN SIGNATURE:", medGray, white));
                Cell signeeValueCell = new Cell();
                signeeValueCell.setBorder(new SolidBorder(new DeviceRgb(200, 200, 200), 0.5f));
                signeeValueCell.setPadding(5);
                signeeValueCell.setBackgroundColor(lightGray);
                byte[] sigData = SignatureController.getSignatureByUserId(Session.currentUserId);
                if (sigData != null) {
                    com.itextpdf.layout.element.Image sigImage = new com.itextpdf.layout.element.Image(
                            ImageDataFactory.create(sigData)
                    );
                    sigImage.setWidth(150);
                    sigImage.setHeight(50);
                    signeeValueCell.add(sigImage);
                } else {
                    signeeValueCell.add(new Paragraph(Session.currentUsername != null ? Session.currentUsername : "")
                            .setFontColor(gold)
                            .setFontSize(10));
                }
                signeeRow.addCell(signeeValueCell);
                mainCell.add(signeeRow);

                headerTable.addCell(mainCell);

                document.add(headerTable);
                document.close();

                lblSlipStatusMessage.setText("PDF SAVED SUCCESSFULLY");

                String empName = txtSlipEmployeeName.getText();
                String purpose = txtSlipPurpose.getText();
                ActivityLogger.log(
                        "PRINT_SLIP",
                        "Printed pass slip for " + empName + " — Purpose: " + purpose,
                        selectedEmployeeId
                );

            } catch (Exception e) {
                e.printStackTrace();
                lblSlipStatusMessage.setText("FAILED TO SAVE PDF");
            }
        }
    }

    private Cell createLabelCell(String text, DeviceRgb borderColor, DeviceRgb bgColor) {
        return new Cell()
                .add(new Paragraph(text)
                        .setFontColor(new DeviceRgb(80, 80, 80))
                        .setFontSize(8)
                        .setBold())
                .setBorder(new SolidBorder(borderColor, 0.5f))
                .setPadding(5)
                .setBackgroundColor(bgColor);
    }

    private Cell createValueCell(String text, DeviceRgb fontColor, DeviceRgb bgColor) {
        return new Cell()
                .add(new Paragraph(text != null ? text : "")
                        .setFontColor(fontColor)
                        .setFontSize(10))
                .setBorder(new SolidBorder(new DeviceRgb(200, 200, 200), 0.5f))
                .setPadding(5)
                .setBackgroundColor(bgColor);
    }

}
