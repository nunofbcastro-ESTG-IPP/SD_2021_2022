package estg.ipp.pt.sd_grupo6_client.ScreensController.passenger;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import estg.ipp.pt.sd_grupo6_client.Main;
import estg.ipp.pt.sd_grupo6_client.ScreensController.SceneController;
import estg.ipp.pt.sd_grupo6_client.ScreensController.SceneName;
import estg.ipp.pt.sd_grupo6_client.Utils.SynchronizedArrayList;
import estg.ipp.pt.sd_grupo6_client.localTrafficManager.LocalTrafficServer;
import estg.ipp.pt.sd_grupo6_client.models.Line;
import estg.ipp.pt.sd_grupo6_client.models.Notification;
import estg.ipp.pt.sd_grupo6_client.models.User;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Controller que gere as interações entre as diversas páginas do passageiro
 */
public class HomePassengerController {
    User passenger;
    @FXML
    BorderPane borderPane;
    ArrayList<Socket> socketList = new ArrayList<>();

    private SynchronizedArrayList<Notification> notificationsList;

    FXMLLoader loaderLines = new FXMLLoader(Main.class.getResource(SceneName.passengerLines));

    FXMLLoader loaderSubscribe = new FXMLLoader(Main.class.getResource(SceneName.passengerSubscribe));

    Parent rootLines = null;
    Parent rootSubscribe = null;

    LinesPassengerController linesPassengerController;
    SubscribeController subscribeController;

    /**
     * Ação executada ao mover para a página "Ver linhas".
     * Antes de carregar a página é necessário  estabelecer ligação com os gestores locais
     * ativos e verificar que linhas estão abertas.
     * @throws IOException exceção enviada caso haja algum erro no load
     */
    private void changeToLinesScreen() throws IOException {
        LocalTrafficServer localTrafficServer = new LocalTrafficServer();
        Gson g = new Gson();
        String jsonLines = localTrafficServer.getAllLines();

        ArrayList<Line> lines = Line.StringJsonToLines(g.fromJson(jsonLines, JsonArray.class));

        for (int i = 0; i < passenger.getLines().size(); i++) {
            //percorrer a lista de linhas da base de dados para encontrar o host e a porta do gestor da linha
            //Ao mesmo tempo verifica se a linha está aberta(line.getStatus == true)
            for (Line line : lines) {
                if (line.getStatus() && Objects.equals(passenger.getLines().get(i), line.getNome())) {
                    try {
                        socketList.add(new Socket(line.getHost(), line.getPort()));
                        break;
                    } catch (IOException e) {
                        System.out.println("Não existe nenhum gestor ativo na linha  " + line.getNome());
                    }
                }
            }
        }

        if (rootLines == null) {
            rootLines = loaderLines.load();
        }

        linesPassengerController = loaderLines.getController();

        if (socketList.size() == 0) {
            linesPassengerController.run(passenger, null, notificationsList);
        } else {
            linesPassengerController.run(passenger, socketList, notificationsList);
        }
        borderPane.setRight(rootLines);
    }

    /**
     * Ação executada ao mover para a página "Subscrever a novas linhas"
     * @throws IOException exceção enviada caso haja algum erro no load
     */
    private void changeToSubscribeScreen() throws IOException {
        this.notificationsList.clearList();

        if (rootSubscribe == null) {
            rootSubscribe = loaderSubscribe.load();
        }

        subscribeController = loaderSubscribe.getController();

        subscribeController.run(passenger, socketList);

        borderPane.setRight(rootSubscribe);
    }

    public void run(User passenger, SynchronizedArrayList<Notification> notificationsList, Stage stage) throws IOException {
        this.passenger = passenger;
        this.notificationsList = notificationsList;
        changeToLinesScreen();

        stage.setOnCloseRequest(event -> closeSockets());
    }

    /**
     * Fecha todos os sockets da lista "socketList" e limpa a lista.
     */
    private void closeSockets() {
        Platform.runLater(() -> {
            for (Socket socket : socketList) {
                try {
                    socket.close();
                } catch (IOException ignored) {
                }
            }
            socketList = new ArrayList<>();
        });
    }

    /**
     * Interação com o botão do menu lateral "Sair"
     * @param event evento do botão
     * @throws IOException exceção recebida pela função "SceneController.Authentication"
     */
    @FXML
    protected void SignoutClick(ActionEvent event) throws IOException {
        closeSockets();
        SceneController.Authentication((Stage) ((Node) event.getSource()).getScene().getWindow());
    }

    /**
     * Interação com o botão do menu lateral "Ver linhas"
     * @param event evento do botão
     * @throws IOException exceção recebida pela função "changeToLinesScreen"
     */
    @FXML
    protected void OpenLinesScreen(ActionEvent event) throws IOException {
        changeToLinesScreen();
    }

    /**
     * Interação com o botão do menu lateral "Subscrever a novas linhas"
     * @param event evento do botão
     * @throws IOException exceção recebida pela função "changeToSubscribeScreen"
     */
    @FXML
    protected void OpenSubscribeScreen(ActionEvent event) throws IOException {
        closeSockets();
        changeToSubscribeScreen();
    }
}
