package estg.ipp.pt;

import estg.ipp.pt.models.Notification;
import estg.ipp.pt.screenscontroller.SceneController;
import estg.ipp.pt.utlis.SynchronizedArrayList;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    private SynchronizedArrayList<Notification> notificationsList = new SynchronizedArrayList<>();
    private static Server server;

    /**
     * Inicializa o servidor e abre a screen principal
     * @param stage local onde serão criadas as janelas da aplicação
     * @throws IOException exceção recebida de outras screens
     */
    @Override
    public void start(Stage stage) throws IOException {
        SceneController.Home(stage, notificationsList);

        server = new Server(notificationsList);
        server.start();
        stage.setOnCloseRequest(event -> server.doStop());
    }

    public static void main(String[] args) {
        launch();
        server.doStop();
    }
}