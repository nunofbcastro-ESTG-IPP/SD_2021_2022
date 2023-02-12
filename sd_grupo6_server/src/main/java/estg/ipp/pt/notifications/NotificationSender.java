package estg.ipp.pt.notifications;

import com.google.gson.JsonObject;
import estg.ipp.pt.models.Notification;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;

public class NotificationSender extends Thread {
    private Socket socket = null;
    private Notification notification;
    private PrintWriter out = null;

    public NotificationSender(Notification notification) {
        super("client.client.NotificationSender");
        this.notification = notification;
    }

    /**
     * Envia uma notificação para os gestores locais
     */
    public void run() {
        try {
            socket = new Socket("127.0.0.1", 2050);
            JsonObject reportObject = new JsonObject();

            reportObject.addProperty("NotificationDate", notification.getNotificationDate());
            reportObject.addProperty("UserEmail", notification.getUserEmail());
            reportObject.addProperty("Message", notification.getMessage());
            reportObject.addProperty("Line", notification.getLine());

            out = new PrintWriter(socket.getOutputStream(), true);

            out.println(Arrays.toString(new String[]{"SendNotificationToLocals", reportObject.toString()}));
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (socket != null) {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }

        if (out != null) {
            out.close();
        }
    }
}
