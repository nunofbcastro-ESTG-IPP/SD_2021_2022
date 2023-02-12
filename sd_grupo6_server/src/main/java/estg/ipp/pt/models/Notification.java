package estg.ipp.pt.models;


public class Notification {
    private String notificationDate;
    private String userEmail;
    private String message;
    private String line;

    public Notification() {
    }

    public Notification(String notificationDate, String userEmail, String message, String line) {
        this.notificationDate = notificationDate;
        this.userEmail = userEmail;
        this.message = message;
        this.line = line;
    }

    public String getNotificationDate() {
        return notificationDate;
    }

    public void setNotificationDate(String notificationDate) {
        this.notificationDate = notificationDate;
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
}
