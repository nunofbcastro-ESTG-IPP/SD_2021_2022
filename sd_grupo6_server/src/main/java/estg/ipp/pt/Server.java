package estg.ipp.pt;

import estg.ipp.pt.models.Notification;
import estg.ipp.pt.utlis.SynchronizedArrayList;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

/**
 * Thread do Servidor que recebe novos clientes
 */
public class Server extends Thread {
    public static List<WorkerThread> lista = new ArrayList<>();
    private SynchronizedArrayList<Notification> notificationsList;

    private boolean doStop = false;

    /**
     * Altera o estado da variável doStop indicando que o servidor deve ser fechado
     */
    public synchronized void doStop() {
        this.doStop = true;
    }

    /**
     * Verifica se o servidor deve ser fechado ou não
     * @return true caso o servidor continue a correr, caso contrário, false
     */
    private synchronized boolean keepRunning() {
        return !this.doStop;
    }

    public Server(SynchronizedArrayList<Notification> notificationsList) {
        this.notificationsList = notificationsList;
    }

    /**
     * Recebe novas conexões, criando uma thread para interagir com essa conexão
     */
    @Override
    public void run() {
        int port = 2050;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            serverSocket.setSoTimeout(5000);

            System.out.println("Server is Up");
            while (keepRunning()) {
                try {
                    Socket socket = serverSocket.accept();
                    lista.add(new WorkerThread(socket, notificationsList));
                    System.out.println("New client connected!");
                    lista.get(lista.size() - 1).start();
                } catch (SocketTimeoutException ignored) {
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("Server is Down");
        } catch (IOException e) {
            System.err.println("Could not listen on port " + port);
            System.exit(-1);
        }
    }
}
