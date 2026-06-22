package models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PassSlip {

    private int id;
    private int employeeId;
    private String employeeName;
    private String department;
    private String reason;
    private LocalDateTime timeOut;
    private LocalDateTime timeIn;
    private LocalDateTime estimatedReturn;
    private long durationMinutes;
    private String status;

    public PassSlip() {}

    public PassSlip(
            int id,
            int employeeId,
            String employeeName,
            String department,
            String reason,
            LocalDateTime timeOut,
            LocalDateTime timeIn,
            LocalDateTime estimatedReturn,
            long durationMinutes,
            String status
    ) {
        this.id = id;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.department = department;
        this.reason = reason;
        this.timeOut = timeOut;
        this.timeIn = timeIn;
        this.estimatedReturn = estimatedReturn;
        this.durationMinutes = durationMinutes;
        this.status = status;
    }

    public int getId() { return id; }
    public int getEmployeeId() { return employeeId; }
    public String getEmployeeName() { return employeeName; }
    public String getDepartment() { return department; }
    public String getReason() { return reason; }
    public LocalDateTime getTimeOut() { return timeOut; }
    public LocalDateTime getTimeIn() { return timeIn; }
    public LocalDateTime getEstimatedReturn() { return estimatedReturn; }
    public long getDurationMinutes() { return durationMinutes; }
    public String getStatus() { return status; }

    public void setId(int id) { this.id = id; }
    public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public void setDepartment(String department) { this.department = department; }
    public void setReason(String reason) { this.reason = reason; }
    public void setTimeOut(LocalDateTime timeOut) { this.timeOut = timeOut; }
    public void setTimeIn(LocalDateTime timeIn) { this.timeIn = timeIn; }
    public void setEstimatedReturn(LocalDateTime estimatedReturn) { this.estimatedReturn = estimatedReturn; }
    public void setDurationMinutes(long durationMinutes) { this.durationMinutes = durationMinutes; }
    public void setStatus(String status) { this.status = status; }

    public String getDurationText() {
        if (durationMinutes < 0) return "N/A";
        long hours = durationMinutes / 60;
        long mins = durationMinutes % 60;
        return hours + " hrs " + mins + " mins";
    }

    public String getEstimatedReturnText() {
        if (estimatedReturn == null) return "N/A";
        return estimatedReturn.format(DateTimeFormatter.ofPattern("hh:mm a"));
    }
}
