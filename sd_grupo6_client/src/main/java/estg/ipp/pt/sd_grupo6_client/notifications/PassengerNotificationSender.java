package estg.ipp.pt.sd_grupo6_client.notifications;

import com.google.gson.JsonObject;
import estg.ipp.pt.sd_grupo6_client.models.Notification;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class PassengerNotificationSender extends Thread {
    private Socket socket;
    private Notification notification;
    private PrintWriter out = null;

    public PassengerNotificationSender(Socket socket, Notification notification) {
        super("client.client.PassengerNotificationSender");
        this.socket = socket;
        this.notification = notification;
    }

    /**
     * Envia uma notificação para o gestor local que a gere
     */
    public void run() {
        try {
            JsonObject notificationObject = new JsonObject();

            notificationObject.addProperty("NotificationDate", notification.getNotificationDateTime());
            notificationObject.addProperty("UserEmail", notification.getUserEmail());
            notificationObject.addProperty("Message", notification.getMessage());
            notificationObject.addProperty("Line", notification.getLine());

            out = new PrintWriter(socket.getOutputStream(), true);
            out.println(notificationObject);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
