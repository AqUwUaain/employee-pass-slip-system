package utils;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TimerService {

    public static class OutRecord {
        public final int employeeId;
        public final String employeeName;
        public final LocalDateTime timeOut;

        public OutRecord(int employeeId, String employeeName, LocalDateTime timeOut) {
            this.employeeId = employeeId;
            this.employeeName = employeeName;
            this.timeOut = timeOut;
        }

        public long getElapsedMinutes() {
            return ChronoUnit.MINUTES.between(timeOut, LocalDateTime.now());
        }

        public String getElapsedText() {
            long totalMinutes = getElapsedMinutes();
            long hours = totalMinutes / 60;
            long mins = totalMinutes % 60;
            return String.format("%dh %02dm", hours, mins);
        }
    }

    private static final Map<Integer, OutRecord> outRecords =
            new ConcurrentHashMap<>();

    private static Timeline refreshTimer;

    private static Runnable onUpdate;

    public static void setOnUpdateCallback(Runnable callback) {
        onUpdate = callback;
    }

    public static void startAutoRefresh() {
        if (refreshTimer != null) {
            refreshTimer.stop();
        }

        refreshTimer = new Timeline(
                new KeyFrame(Duration.seconds(30), event -> {
                    if (onUpdate != null) {
                        Platform.runLater(onUpdate);
                    }
                })
        );
        refreshTimer.setCycleCount(Timeline.INDEFINITE);
        refreshTimer.play();
    }

    public static void stopAutoRefresh() {
        if (refreshTimer != null) {
            refreshTimer.stop();
            refreshTimer = null;
        }
    }

    public static void markOut(int employeeId, String employeeName) {
        outRecords.put(
                employeeId,
                new OutRecord(employeeId, employeeName, LocalDateTime.now())
        );
    }

    public static void markReturned(int employeeId) {
        outRecords.remove(employeeId);
    }

    public static Map<Integer, OutRecord> getOutRecords() {
        return outRecords;
    }

    public static int getOutCount() {
        return outRecords.size();
    }
}
