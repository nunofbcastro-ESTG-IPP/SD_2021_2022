package estg.ipp.pt.sd_grupo6_client.models;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class Notification {
    private String notificationDateTime;
    private String userEmail;
    private String message;
    private String line;

    public Notification(String notificationDateTime, String userEmail, String message, String line) {
        this.notificationDateTime = notificationDateTime;
        this.userEmail = userEmail;
        this.message = message;
        this.line = line;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    public String getNotificationDateTime() {
        return notificationDateTime;
    }

    public void setNotificationDateTime(String notificationDateTime) {
        this.notificationDateTime = notificationDateTime;
    }

    public static Notification StringJsonToReport(String reportJson) {
        Gson g = new Gson();
        JsonObject reportJsonObject = g.fromJson(reportJson, JsonObject.class);

        String notificationDate = reportJsonObject.get("NotificationDate").getAsString();
        String userEmail = reportJsonObject.get("UserEmail").getAsString();
        String message = reportJsonObject.get("Message").getAsString();
        String line = reportJsonObject.get("Line").getAsString();

        return new Notification(notificationDate, userEmail, message, line);
    }
}
