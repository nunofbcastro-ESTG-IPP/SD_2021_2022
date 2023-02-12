package estg.ipp.pt.sd_grupo6_client.notifications;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import estg.ipp.pt.sd_grupo6_client.Utils.SynchronizedArrayList;
import estg.ipp.pt.sd_grupo6_client.models.Notification;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class PassengerNotificationReceiver extends Thread {
    private Socket socket;
    private SynchronizedArrayList<Notification> notificationsList;
    private BufferedReader in;

    public PassengerNotificationReceiver(Socket socket, SynchronizedArrayList<Notification> notificationsList) {
        super("client.client.PassengerNotificationReceiver");
        this.socket = socket;
        this.notificationsList = notificationsList;
    }

    public Socket getSocket() {
        return socket;
    }

    /**
     * Recebe ums lista de notificações de um gestor local
     */
    public void run() {
        try {
            String inputLine;
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            while (!socket.isClosed()) {
                if (in.ready()) {
                    inputLine = in.readLine();

                    if (inputLine != null) {
                        Gson g = new Gson();
                        JsonObject jo = g.fromJson(inputLine, JsonObject.class);

                        Notification notification = new Notification(
                                jo.get("NotificationDate").getAsString(),
                                jo.get("UserEmail").getAsString(),
                                jo.get("Message").getAsString(),
                                jo.get("Line").getAsString()
                        );
                        this.notificationsList.add(notification);
                    }
                }
            }

            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
