package models;

import java.time.LocalDateTime;

public class PassSlip {

    private int id;
    private int employeeId;
    private String employeeName;
    private String department;
    private String reason;
    private LocalDateTime timeOut;
    private LocalDateTime timeIn;
    private long durationMinutes;
    private String status;

    public PassSlip(
            int id,
            int employeeId,
            String employeeName,
            String department,
            String reason,
            LocalDateTime timeOut,
            LocalDateTime timeIn,
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
    public long getDurationMinutes() { return durationMinutes; }
    public String getStatus() { return status; }

    public String getDurationText() {
        if (durationMinutes < 0) return "N/A";
        long hours = durationMinutes / 60;
        long mins = durationMinutes % 60;
        return hours + " hrs " + mins + " mins";
    }
}
