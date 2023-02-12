package estg.ipp.pt.sd_grupo6_client.localTrafficManager;

import com.google.gson.Gson;
import estg.ipp.pt.sd_grupo6_client.Utils.SynchronizedArrayList;
import estg.ipp.pt.sd_grupo6_client.models.Notification;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.Objects;

public class WorkerThread extends Thread {
    private final Socket socket;
    private Socket centralServerSocket;
    private SynchronizedArrayList<Notification> managerNotificationsList;
    private PrintWriter out = null;
    private PrintWriter centralServerOut = null;
    private String serverType;
    private boolean shouldStop = false;

    Gson g = new Gson();

    public WorkerThread(Socket socket, String serverType, SynchronizedArrayList<Notification> managerNotificationsList) {
        super("server.WorkerThread");
        this.socket = socket;
        this.serverType = serverType;
        this.managerNotificationsList = managerNotificationsList;
    }

    public WorkerThread(Socket socket, Socket centralServerSocket, String serverType, SynchronizedArrayList<Notification> managerNotificationsList) {
        super("server.WorkerThread");
        this.socket = socket;
        this.centralServerSocket = centralServerSocket;
        this.serverType = serverType;
        this.managerNotificationsList = managerNotificationsList;
    }

    public void sendMessage(String message) {
        out.println(message);
    }

    /**
     * Dependendo do tipo recebido na criação liga uma conexão com o servidor ou com um passageiro
     */
    public void run() {
        if (Objects.equals(serverType, "centralServer")) {
            centralServer();
        } else {
            localServer();
        }
    }

    /**
     * Para a thread que está a correr.
     */
    public void stopThread() {
        shouldStop = true;
    }

    /**
     * Estabelece um meio de comunicação entre o gestor local ativo e o servidor
     */
    public void centralServer() {
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String input;
            String[] originalLine;

            while (!shouldStop) {
                if (in.ready()) {
                    input = in.readLine();

                    if (input != null) {
                        originalLine = input.substring(1, input.length() - 1).split(", ");
                        if (originalLine[0].equals("SendNotificationToLocals")) {
                            String host = originalLine[2];
                            int port = Integer.parseInt(originalLine[3]);
                            int lastPort = -1;
                            for (WorkerThread user : LocalTrafficServer.lista) {
                                if (user.socket.getInetAddress().toString().substring(1).equals(host) && user.socket.getLocalPort() == port) {
                                    user.sendMessage(originalLine[1]);
                                    if (user.socket.getLocalPort() != lastPort) {
                                        this.managerNotificationsList.add(Notification.StringJsonToReport(originalLine[1]));
                                        lastPort = user.socket.getLocalPort();
                                    }
                                }
                            }
                        } else {
                            out.println(originalLine[0]);
                        }
                    }
                }
            }

            LocalTrafficServer.lista.remove(this);
            out.close();
            in.close();
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Estabelece um meio de comunicação entre o gestor local ativo e um passageiro
     */
    public void localServer() {
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            centralServerOut = new PrintWriter(centralServerSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String input;
            String originalLine;

            while (!shouldStop) {
                if (in.ready()) {
                    input = in.readLine();

                    if (input != null) {
                        originalLine = input;
                        LocalTrafficServer.incrementWarningsCounter();
                        for (int i = 1; i < LocalTrafficServer.lista.size(); i++) {
                            LocalTrafficServer.lista.get(i).sendMessage(originalLine);
                            LocalTrafficServer.incrementPassengersWarnedCounter();
                        }
                        this.managerNotificationsList.add(Notification.StringJsonToReport(originalLine));

                        centralServerOut.println(Arrays.toString(new String[]{"SendNotification", originalLine}));
                    }
                }
            }

            LocalTrafficServer.lista.remove(this);
            out.close();
            in.close();
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
