// Converts the raw timestamp string stored on a ScanRecord (produced by
// LocalDateTime.now().toString() in MainView, e.g. "2026-07-09T14:32:07.123")
// into a human-friendly relative string for the History view, e.g. "2h ago".
package main.java.com.byteanarchists.codeguard.util;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeParseException;

public class TimeFormatter {

    public static String humanize(String isoTimestamp) {
        if (isoTimestamp == null || isoTimestamp.isBlank()) {
            return "Unknown time";
        }

        LocalDateTime then;
        try {
            then = LocalDateTime.parse(isoTimestamp);
        } catch (DateTimeParseException e) {
            return isoTimestamp;
        }

        LocalDateTime now = LocalDateTime.now();
        long seconds = ChronoUnit.SECONDS.between(then, now);

        if (seconds < 0) {
            return "Just now";
        }
        if (seconds < 60) {
            return "Just now";
        }
        long minutes = seconds / 60;
        if (minutes < 60) {
            return minutes + "m ago";
        }
        long hours = minutes / 60;
        if (hours < 24) {
            return hours + "h ago";
        }
        long days = ChronoUnit.DAYS.between(then.toLocalDate(), now.toLocalDate());
        if (days == 1) {
            return "Yesterday";
        }
        if (days < 7) {
            return days + " days ago";
        }

        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun",
                            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        return months[then.getMonthValue() - 1] + " " + then.getDayOfMonth() + ", " + then.getYear();
    }
}