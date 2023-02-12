package estg.ipp.pt.screenscontroller.admin;

import estg.ipp.pt.Main;
import estg.ipp.pt.models.Notification;
import estg.ipp.pt.screenscontroller.SceneName;
import estg.ipp.pt.utlis.SynchronizedArrayList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;

import java.io.IOException;

/**
 * Controller que gere as interações entre as diversas páginas do servidor
 */
public class HomeController {
    @FXML
    BorderPane borderPane;
    private SynchronizedArrayList<Notification> notificationsList;

    FXMLLoader loaderLines = new FXMLLoader(Main.class.getResource(SceneName.lines));
    FXMLLoader loaderNotifyLines = new FXMLLoader(Main.class.getResource(SceneName.notifyLines));
    FXMLLoader loaderUsers = new FXMLLoader(Main.class.getResource(SceneName.users));

    Parent rootLines = null, rootNotifyLines = null, rootUsers = null;

    LinesController linesController;
    NotifyLinesController notifyLinesController;
    UsersController usersController;

    public void run(SynchronizedArrayList<Notification> notificationsList) throws IOException {
        this.notificationsList = notificationsList;
        changeToLinesScreen();
    }

    /**
     * Ação executada ao mover para a página "LinesController"
     * @throws IOException exceção enviada caso haja algum erro no load
     */
    private void changeToLinesScreen() throws IOException {
        if (rootLines == null) {
            rootLines = loaderLines.load();
        }

        linesController = loaderLines.getController();
        linesController.run();

        borderPane.setRight(rootLines);
    }

    /**
     * Ação executada ao mover para a página "NotifyLinesController"
     * @throws IOException exceção enviada caso haja algum erro no load
     */
    private void changeToNotifyLinesScreen() throws IOException {
        if (rootNotifyLines == null) {
            rootNotifyLines = loaderNotifyLines.load();
        }

        notifyLinesController = loaderNotifyLines.getController();
        notifyLinesController.run(notificationsList);

        borderPane.setRight(rootNotifyLines);
    }

    /**
     * Ação executada ao mover para a página "UsersController"
     * @throws IOException exceção enviada caso haja algum erro no load
     */
    private void changeToUsersScreen() throws IOException {
        if (rootUsers == null) {
            rootUsers = loaderUsers.load();
        }

        usersController = loaderUsers.getController();
        usersController.run();

        borderPane.setRight(rootUsers);
    }

    /**
     * Interação com o botão do menu lateral "Gerir linhas"
     * @param event evento do botão
     * @throws IOException exceção recebida pela função "changeToLinesScreen"
     */
    @FXML
    protected void OpenLinesManagement(ActionEvent event) throws IOException {
        changeToLinesScreen();
    }

    /**
     * Interação com o botão do menu lateral "Notificar linhas"
     * @param event evento do botão
     * @throws IOException exceção recebida pela função "changeToNotifyLinesScreen"
     */
    @FXML
    protected void OpenNotifyLines(ActionEvent event) throws IOException {
        changeToNotifyLinesScreen();
    }

    /**
     * Interação com o botão do menu lateral "Gerir utilizadores"
     * @param event evento do botão
     * @throws IOException exceção recebida pela função "changeToUsersScreen"
     */
    @FXML
    protected void OpenUsersManagement(ActionEvent event) throws IOException {
        changeToUsersScreen();
    }
}
