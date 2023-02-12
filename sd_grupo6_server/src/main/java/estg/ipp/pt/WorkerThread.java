package estg.ipp.pt;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import estg.ipp.pt.models.Report;
import estg.ipp.pt.models.Notification;
import estg.ipp.pt.database.Database;
import estg.ipp.pt.models.User;
import estg.ipp.pt.utlis.Convert;
import estg.ipp.pt.utlis.SynchronizedArrayList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Thread que conecta o servidor com um utilizador
 */
public class WorkerThread extends Thread {
    private Socket socket;
    private SynchronizedArrayList<Notification> notificationsList;
    PrintWriter out = null;

    public WorkerThread(Socket socket, SynchronizedArrayList<Notification> notificationsList) {
        super("server.WorkerThread");
        this.socket = socket;
        this.notificationsList = notificationsList;
    }

    /**
     * Envia mensagem para o utilizador conectado
     * @param message mensagem a ser enviada
     */
    public void sendMessage(String message) {
        out.println(message);
    }

    /**
     * Recebe uma flag que indica o que funcionalidade deve ser executada, efetuando um pedido à base
     * de dados ou então enviando uma mensagem para o utilizador conectado
     */
    public void run() {
        try {
            Database database = new Database(notificationsList);
            Gson g = new Gson();

            String input;
            String[] originalLine;
            out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            while ((input = in.readLine()) != null) {
                originalLine = input.substring(1, input.length() - 1).split(", ", 5);
                System.out.println(originalLine[0]);
                switch (originalLine[0]) {
                    case "Login":
                        out.println(database.loginUser(originalLine[1], originalLine[2]));
                        break;
                    case "Register":
                        List<String> lines = Arrays.asList(originalLine[4].substring(1, originalLine[4].length() - 1).split(", "));
                        User passenger = new User(originalLine[1], originalLine[2], originalLine[3], lines);
                        out.println(database.registerUser(passenger));
                        break;
                    case "SendNotification":
                        JsonObject notificationObjectFromLocal = g.fromJson(originalLine[1], JsonObject.class);
                        Notification notification = new Notification(
                                notificationObjectFromLocal.get("NotificationDate").getAsString(),
                                notificationObjectFromLocal.get("UserEmail").getAsString(),
                                notificationObjectFromLocal.get("Message").getAsString(),
                                notificationObjectFromLocal.get("Line").getAsString());

                        out.println(database.insertNotification(notification));
                        break;
                    case "SendNotificationToLocals":
                        JsonObject notificationObjectFromServer = g.fromJson(originalLine[1], JsonObject.class);
                        JsonObject line = g.fromJson(database.getLine(notificationObjectFromServer.get("Line").getAsString()), JsonObject.class);
                        String host = line.get("Host").getAsString();
                        String port = line.get("Port").getAsString();

                        for (WorkerThread localManager : Server.lista) {
                            if (Server.lista.indexOf(localManager) != Server.lista.size() - 1) {
                                localManager.sendMessage(Arrays.toString(new String[]{"SendNotificationToLocals", originalLine[1], host, port}));
                            }
                        }
                        Server.lista.remove(this);
                        break;
                    case "GetNotificationsByLine":
                        out.println(database.getNotificationsFromLine(originalLine[1]));
                        break;
                    case "SendReport":
                        JsonObject relatorioObject = g.fromJson(originalLine[1], JsonObject.class);
                        Report report = new Report(
                                LocalDateTime.parse(relatorioObject.get("ReportDate").getAsString()),
                                relatorioObject.get("Line").getAsString(),
                                Integer.parseInt(relatorioObject.get("TotalWarnings").getAsString()),
                                Integer.parseInt(relatorioObject.get("TotalPassengersWarned").getAsString()));
                        out.println(database.insertReport(report));
                        break;
                    case "LineStatus":
                        out.println(database.changeLineStatus(originalLine[1], Boolean.parseBoolean(originalLine[2])));
                        break;
                    case "Line":
                        out.println(database.getLine(originalLine[1]));
                        break;
                    case "Lines":
                        out.println(database.getLines());
                        break;
                    case "AddLine":
                        out.println(database.insertPassengerLine(originalLine[1], originalLine[2]));
                        break;
                    case "RemoveLine":
                        out.println(database.removePassengerLine(originalLine[1], originalLine[2]));
                        break;
                    case "ChangeLineHostPort":
                        String hostValue = socket.getInetAddress().getHostAddress();
                        out.println(database.changeLineHostPort(originalLine[1], hostValue, Convert.StringToInt(originalLine[2], null)));
                        break;
                }
            }

            Server.lista.remove(this);
            out.close();
            in.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
