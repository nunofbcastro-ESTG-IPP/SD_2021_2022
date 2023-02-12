package estg.ipp.pt.sd_grupo6_client.passenger;


import estg.ipp.pt.sd_grupo6_client.Utils.SynchronizedArrayList;
import estg.ipp.pt.sd_grupo6_client.models.User;
import estg.ipp.pt.sd_grupo6_client.models.Notification;
import estg.ipp.pt.sd_grupo6_client.notifications.PassengerNotificationReceiver;
import estg.ipp.pt.sd_grupo6_client.notifications.PassengerNotificationSender;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

public class PassengerTcp {
    private ArrayList<PassengerNotificationReceiver> receivers;

    public PassengerTcp() {
        this.receivers = new ArrayList<>();
    }

    /**
     * Conecta com o servidor para tentar efetuar login
     * @param loginData dados do login
     * @return Utilizador com os dados enviados por parâmetro ou null em caso de erro
     */
    public String connectWithServer(String[] loginData) {
        Socket s = null;
        PrintWriter out = null;
        BufferedReader in = null;

        try {
            s = new Socket("127.0.0.1", 2050);
            out = new PrintWriter(s.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            out.println(Arrays.toString(new String[]{"Login", loginData[0], loginData[1]}));
            String input;

            if ((input = in.readLine()) != null) {
                out.close();
                in.close();
                s.close();

                return input;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (s != null) {
            try {
                s.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (out != null) {
            out.close();
        }
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    /**
     * Conecta com o servidor para tentar efetuar registo
     * @param user dados do registo
     * @return Utilizador registado ou null em caso de erro
     */
    public String connectWithServer(User user) {
        Socket s = null;
        PrintWriter out = null;
        BufferedReader in = null;

        try {
            s = new Socket("127.0.0.1", 2050);
            out = new PrintWriter(s.getOutputStream(), true);
            out.println(Arrays.toString(new String[]{"Register", user.getNome(), user.getEmail(), user.getPassword(), user.getLines().toString()}));
            in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            String input;

            if ((input = in.readLine()) != null) {
                out.close();
                in.close();
                s.close();

                return input;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


        if (s != null) {
            try {
                s.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (out != null) {
            out.close();
        }
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    /**
     * Conecta com o servidor para obter o histórico de uma linha
     * @param line linha para obter histórico
     * @return Histórico da linha
     */
    public String getReportsByLineFromServer(String line) {
        Socket s = null;
        PrintWriter out = null;
        BufferedReader in = null;

        try {
            s = new Socket("127.0.0.1", 2050);
            out = new PrintWriter(s.getOutputStream(), true);
            out.println(Arrays.toString(new String[]{"GetNotificationsByLine", line}));
            in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            String input;

            if ((input = in.readLine()) != null) {
                out.close();
                in.close();
                s.close();

                return input;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        if (s != null) {
            try {
                s.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (out != null) {
            out.close();
        }
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    /**
     * Envia a notificação recebida para o gestor local recebido
     * @param notification notificação a enviar
     * @param socket conexão com o gestor local
     */
    public void sendNotification(Notification notification, Socket socket) {
        PassengerNotificationSender sender;
        sender = new PassengerNotificationSender(socket, notification);
        sender.start();
    }

    /**
     * Recebe uma lista de notificações de um gestor local
     * @param notificationsList Lista de notificações
     * @param socketList Lista de conexões
     */
    public void receiveNotification(SynchronizedArrayList<Notification> notificationsList, ArrayList<Socket> socketList) {
        PassengerNotificationReceiver receiver = null;
        boolean alreadyAdded = false;

        for (Socket socket : socketList) {
            if (socket != null) {
                for (PassengerNotificationReceiver receiver1 : receivers) {
                    if (receiver1.getSocket().getInetAddress() == socket.getInetAddress() && receiver1.getSocket().getPort() == socket.getPort()) {
                        alreadyAdded = true;
                    }
                }

                if (!alreadyAdded) {
                    receiver = new PassengerNotificationReceiver(socket, notificationsList);
                    receiver.start();

                    this.receivers.add(receiver);
                }
            }
        }
    }
}
