package estg.ipp.pt.sd_grupo6_client.localTrafficManager;

import com.google.gson.JsonObject;
import estg.ipp.pt.sd_grupo6_client.Utils.SynchronizedArrayList;
import estg.ipp.pt.sd_grupo6_client.models.Notification;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LocalTrafficServer extends Thread {
    private Socket centralServerSocket;
    private Socket localManagerSocket;
    static List<WorkerThread> lista = new ArrayList<>();
    private static int passengersWarnedCounter = 0;
    private static int warningsCounter = 0;
    private SynchronizedArrayList<Notification> managerNotificationsList;
    PrintWriter out, outLocalManager;
    int localServerPort;
    String localServerHost;
    private boolean shouldStop = false;
    private String email;

    public LocalTrafficServer() {
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setLocalServerHost(String localServerHost) {
        this.localServerHost = localServerHost;
    }

    public void setLocalServerPort(int localServerPort) {
        this.localServerPort = localServerPort;
    }

    public static int getPassengersWarnedCounter() {
        return passengersWarnedCounter;
    }

    public static void incrementPassengersWarnedCounter() {
        LocalTrafficServer.passengersWarnedCounter += 1;
    }

    public static int getWarningsCounter() {
        return warningsCounter;
    }

    public static void incrementWarningsCounter() {
        LocalTrafficServer.warningsCounter += 1;
    }

    public static void resetCounters() {
        LocalTrafficServer.warningsCounter = 0;
        LocalTrafficServer.passengersWarnedCounter = 0;
    }

    public void stopThread() {
        shouldStop = true;
    }

    /**
     * Estabelece conexão com o servidor e espera por tentativas de conexão por parte dos passageiros.
     */
    public void run() {
        ServerSocket localManagerServerSocket = null;

        try {
            centralServerSocket = new Socket("127.0.0.1", 2050);
            localManagerServerSocket = new ServerSocket(0);
            localManagerServerSocket.setSoTimeout(1000);
            lista.add(new WorkerThread(centralServerSocket, "centralServer", this.managerNotificationsList));
            lista.get(lista.size() - 1).start();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            System.exit(-1);
        } catch (IOException e) {
            System.err.println("Could not listen on port " + this.localServerPort);
            System.exit(-1);
        }
        this.setLocalServerHost("localhost");
        this.setLocalServerPort(localManagerServerSocket.getLocalPort());
        this.setLinePort(email, localManagerServerSocket.getLocalPort());

        ReportThread relatorioThread = new ReportThread(centralServerSocket);
        relatorioThread.start();

        while (!this.shouldStop) {
            try {
                lista.add(new WorkerThread(localManagerServerSocket.accept(), centralServerSocket, "localServer", this.managerNotificationsList));
                lista.get(lista.size() - 1).start();
                System.out.println("connected socket: " + lista.get(lista.size() - 1));
            } catch (SocketTimeoutException ignored) {
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        for (WorkerThread workerThread : lista) {
            workerThread.stopThread();
        }

        try {
            centralServerSocket.close();
            relatorioThread.close();
            localManagerServerSocket.close();
            localManagerSocket.close();
            outLocalManager.close();
        } catch (IOException ignored) {
        }
    }

    /**
     * Envia uma notificação para os passageiros.
     *
     * @param notification notificação a ser enviada
     */
    public void sendNotificationFromLocalToPassengers(Notification notification) {
        try {
            localManagerSocket = new Socket(this.localServerHost, this.localServerPort);
            outLocalManager = new PrintWriter(localManagerSocket.getOutputStream(), true);

            JsonObject notificationObject = new JsonObject();
            notificationObject.addProperty("NotificationDate", notification.getNotificationDateTime());
            notificationObject.addProperty("UserEmail", notification.getUserEmail());
            notificationObject.addProperty("Message", notification.getMessage());
            notificationObject.addProperty("Line", notification.getLine());

            outLocalManager.println(notificationObject);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (localManagerSocket != null) {
            try {
                localManagerSocket.close();
                lista.remove(lista.size() - 1);
            } catch (IOException ignored) {
            }
        }

        if (outLocalManager != null) {
            outLocalManager.close();
        }
    }

    /**
     * Altera o estado de uma linha tendo em conta o estado recebido.
     *
     * @param line   linha a ser alterada.
     * @param status novo estado da linha
     */
    public void changeLineStatus(String line, Boolean status) {
        Socket centralServerSocket = null;
        try {
            centralServerSocket = new Socket("127.0.0.1", 2050);
            out = new PrintWriter(centralServerSocket.getOutputStream(), true);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Could not listen on port ");
        }

        out.println(Arrays.toString(new String[]{"LineStatus", line, status.toString()}));

        if (centralServerSocket != null) {
            try {
                centralServerSocket.close();
            } catch (IOException ignored) {
            }
        }

        if (out != null) {
            out.close();
        }
    }

    /**
     * Obtém a linha a que corresponde o nome recebido
     *
     * @param line nome da linha
     * @return Linha com o nome recebido por parâmetro ou null em caso de erro
     */
    public String getLine(String line) {
        Socket centralServerSocket = null;
        BufferedReader in = null;
        try {
            centralServerSocket = new Socket("127.0.0.1", 2050);
            out = new PrintWriter(centralServerSocket.getOutputStream(), true);

            out.println(Arrays.toString(new String[]{"Line", line}));

            String inputLine;
            in = new BufferedReader(new InputStreamReader(centralServerSocket.getInputStream()));
            if ((inputLine = in.readLine()) != null) {
                out.close();
                in.close();
                centralServerSocket.close();

                return inputLine;
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Could not listen on port ");
        }

        if (centralServerSocket != null) {
            try {
                centralServerSocket.close();
            } catch (IOException ignored) {
            }
        }

        if (out != null) {
            out.close();
        }

        if (in != null) {
            try {
                in.close();
            } catch (IOException ignored) {
            }
        }

        return null;
    }

    /**
     * Obtém todas as linhas do sistema
     *
     * @return Uma lista de linhas em String ou null em caso de erro
     */
    public String getAllLines() {
        Socket centralServerSocket = null;
        BufferedReader in = null;
        try {
            centralServerSocket = new Socket("127.0.0.1", 2050);
            out = new PrintWriter(centralServerSocket.getOutputStream(), true);

            out.println(Arrays.toString(new String[]{"Lines"}));

            String inputLine;
            in = new BufferedReader(new InputStreamReader(centralServerSocket.getInputStream()));
            if ((inputLine = in.readLine()) != null) {
                out.close();
                in.close();
                centralServerSocket.close();

                return inputLine;
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Could not listen on port ");
        }

        if (centralServerSocket != null) {
            try {
                centralServerSocket.close();
            } catch (IOException ignored) {
            }
        }

        if (out != null) {
            out.close();
        }

        if (in != null) {
            try {
                in.close();
            } catch (IOException ignored) {
            }
        }

        return null;
    }

    /**
     * Altera o porto de uma linha
     *
     * @param email email do gestor para posteriormente obter a linha que este gere
     * @param port  novo porto da linha
     */
    public void setLinePort(String email, int port) {
        Socket centralServerSocket;
        PrintWriter out = null;
        BufferedReader in = null;

        try {
            centralServerSocket = new Socket("127.0.0.1", 2050);
            out = new PrintWriter(centralServerSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(centralServerSocket.getInputStream()));
            out.println(Arrays.toString(new String[]{"ChangeLineHostPort", email, String.valueOf(port)}));
            String input;

            if ((input = in.readLine()) != null) {
                System.out.println(input);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (out != null) {
            out.close();
        }

        if (in != null) {
            try {
                in.close();
            } catch (IOException ignored) {
            }
        }
    }

    /**
     * Atribuir uma lista de notificações a um gestor local
     *
     * @param managerNotificationsList lista de notificações
     */
    public void setManagerNotificationsList(SynchronizedArrayList<Notification> managerNotificationsList) {
        this.managerNotificationsList = managerNotificationsList;
    }
}
