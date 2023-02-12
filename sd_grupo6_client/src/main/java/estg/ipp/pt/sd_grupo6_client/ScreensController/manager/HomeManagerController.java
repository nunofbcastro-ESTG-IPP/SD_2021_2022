package estg.ipp.pt.sd_grupo6_client.ScreensController.manager;

import estg.ipp.pt.sd_grupo6_client.Main;
import estg.ipp.pt.sd_grupo6_client.ScreensController.SceneController;
import estg.ipp.pt.sd_grupo6_client.ScreensController.SceneName;
import estg.ipp.pt.sd_grupo6_client.Utils.SynchronizedArrayList;
import estg.ipp.pt.sd_grupo6_client.localTrafficManager.LocalTrafficServer;
import estg.ipp.pt.sd_grupo6_client.models.Notification;
import estg.ipp.pt.sd_grupo6_client.models.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Controller que gere as interações entre as diversas páginas do gestor local
 */
public class HomeManagerController {
    @FXML
    BorderPane borderPane;
    private LocalTrafficServer localTrafficServer;
    private SynchronizedArrayList<Notification> managerNotificationsList;

    public HomeManagerController() {
        localTrafficServer = new LocalTrafficServer();
    }

    public void run(User manager, SynchronizedArrayList<Notification> managerNotificationsList, Stage stage) throws IOException {
        stage.setOnCloseRequest(event -> localTrafficServer.stopThread());
        this.managerNotificationsList = managerNotificationsList;

        changeToLinesScreen(manager);

        localTrafficServer.setEmail(manager.getEmail());
        localTrafficServer.setManagerNotificationsList(managerNotificationsList);

        localTrafficServer.start();
    }

    /**
     * Ação executada ao mover para a página "Gestão da linha".
     *
     * @param manager gestor local atual
     * @throws IOException exceção enviada caso haja algum erro no load
     */
    private void changeToLinesScreen(User manager) throws IOException {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource(SceneName.managerLines));
        Parent root = loader.load();

        LineManagerController controller = loader.getController();
        controller.run(manager, localTrafficServer, this.managerNotificationsList);

        borderPane.setRight(root);
    }

    /**
     * Interação com o botão do menu lateral "Sair"
     *
     * @param event evento do botão
     * @throws IOException exceção recebida pela função "SceneController.Authentication"
     */
    @FXML
    protected void SignoutClick(ActionEvent event) throws IOException {
        localTrafficServer.stopThread();
        SceneController.Authentication((Stage) ((Node) event.getSource()).getScene().getWindow());
    }
}
