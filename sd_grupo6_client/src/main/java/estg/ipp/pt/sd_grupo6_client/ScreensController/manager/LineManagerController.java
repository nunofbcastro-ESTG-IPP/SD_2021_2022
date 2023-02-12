package estg.ipp.pt.sd_grupo6_client.ScreensController.manager;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import estg.ipp.pt.sd_grupo6_client.Utils.SynchronizedArrayList;
import estg.ipp.pt.sd_grupo6_client.localTrafficManager.LocalTrafficServer;
import estg.ipp.pt.sd_grupo6_client.models.User;
import estg.ipp.pt.sd_grupo6_client.models.Notification;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Controller que gere a página "Gestão da linha"
 */
public class LineManagerController {
    private static final int sizeTextLineName = 10;
    @FXML
    private VBox content;
    @FXML
    private Text lineName;
    @FXML
    private TextField messageTextField;
    @FXML
    private Button buttonSend;
    @FXML
    private Button buttonStateLine;
    private User manager;
    private boolean isOpen = true;

    private LocalTrafficServer localTrafficServer = null;

    SynchronizedArrayList<Notification> notificationsList = new SynchronizedArrayList<>();

    /**
     * Listener adicionado à SynchronizedArrayList permitindo adicionar novos elementos e atualizar a lista de forma autónoma
     *
     * @return ListChangeListener a ser adicionado à SynchronizedArrayList
     */
    private ListChangeListener<Notification> getListChangeListener() {
        notificationsList.list();
        return c -> Platform.runLater(
                () -> {
                    if (c.next()) {
                        if (c.wasAdded()) {
                            for (Notification notification : c.getAddedSubList()) {
                                content.getChildren().add(createNotification(notification.getNotificationDateTime(), notification.getLine(), notification.getUserEmail(), notification.getMessage()));
                            }
                        } else {
                            content.getChildren().clear();
                            for (Notification notification : c.getList()) {
                                content.getChildren().add(createNotification(notification.getNotificationDateTime(), notification.getLine(), notification.getUserEmail(), notification.getMessage()));
                            }
                        }
                    }
                }
        );
    }

    /**
     * Ao carregar a página é necessário verificar qual é a linha que o gestor local gere.
     *
     * @param manager            gestor local conectado
     * @param localTrafficServer meio de comunicação utilizado para enviar notificações
     * @param notificationsList  lista onde serão adicionadas novas notificações
     */
    public void run(User manager, LocalTrafficServer localTrafficServer, SynchronizedArrayList<Notification> notificationsList) {
        this.manager = manager;
        this.localTrafficServer = localTrafficServer;
        this.notificationsList = notificationsList;
        notificationsList.addListener(getListChangeListener());

        lineName.setText(this.manager.getLines().get(0));
        isOpen = getLineStatus();
        setStateLine();
    }

    /**
     * Cria uma notificação no ecrã
     *
     * @param notificationDate data da notificação
     * @param line             linha que o gestor local gere
     * @param email            email do gestor local
     * @param message          mensagem da notificação
     */
    private HBox createNotification(String notificationDate, String line, String email, String message) {
        HBox hBox = new HBox();
        hBox.setSpacing(20);
        if (line != null) {
            hBox.getChildren().addAll(
                    new Text(notificationDate), new Text(line), new Text(email), new Text(message)
            );
        } else {
            hBox.getChildren().addAll(
                    new Text(email), new Text(message)
            );
        }
        return hBox;
    }

    /**
     * Interação com o botão "Enviar"
     *
     * @param event evento do botão
     */
    @FXML
    private void onSendClick(ActionEvent event) {
        String notificationMessage = messageTextField.getText();

        if (notificationMessage.length() == 0) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        String passengerEmailAndRole = this.manager.getEmail() + " (" + this.manager.getRole() + ")";

        Notification notification = new Notification(now.format(formatter), passengerEmailAndRole, notificationMessage, lineName.getText());
        localTrafficServer.sendNotificationFromLocalToPassengers(notification);
        messageTextField.setText("");
    }

    /**
     * Atualiza o estado da linha (true ou false) e altera o botão para corresponder à mudança
     */
    private void setStateLine() {
        buttonStateLine.getStyleClass().removeAll(buttonStateLine.getStyleClass());
        LocalTrafficServer localTrafficServer = new LocalTrafficServer();

        if (isOpen) {
            buttonStateLine.setText("Fechar linha");
            buttonStateLine.getStyleClass().add("button");
            buttonStateLine.getStyleClass().add("button4");
            messageTextField.setDisable(false);
            buttonSend.setDisable(false);
            localTrafficServer.changeLineStatus(this.lineName.getText(), true);
        } else {
            buttonStateLine.setText("Abrir linha");
            buttonStateLine.getStyleClass().add("button");
            buttonStateLine.getStyleClass().add("button1");
            messageTextField.setDisable(true);
            buttonSend.setDisable(true);
            localTrafficServer.changeLineStatus(this.lineName.getText(), false);
        }
    }

    /**
     * Interação com o botão de abrir ou fechar a linha
     *
     * @param event evento do botão
     */
    @FXML
    private void onCloseLineClick(ActionEvent event) {
        isOpen = !isOpen;
        setStateLine();
    }

    /**
     * Obter o estado da linha que o gestor local gere
     *
     * @return estado da linha
     */
    private Boolean getLineStatus() {
        LocalTrafficServer localTrafficServer = new LocalTrafficServer();
        String result = localTrafficServer.getLine(this.lineName.getText());

        Gson g = new Gson();
        JsonObject jo = g.fromJson(result, JsonObject.class);

        return Boolean.parseBoolean(jo.get("IsActive").toString());
    }
}
