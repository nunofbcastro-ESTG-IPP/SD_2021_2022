package estg.ipp.pt.screenscontroller.admin;

import estg.ipp.pt.models.Line;
import estg.ipp.pt.database.Database;
import estg.ipp.pt.models.Notification;
import estg.ipp.pt.notifications.NotificationSender;
import estg.ipp.pt.utlis.SynchronizedArrayList;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.controlsfx.control.CheckComboBox;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Controller que gere a página "Notificar Linhas"
 */
public class NotifyLinesController {
    private static final String lines = "Linhas";
    @FXML
    private VBox content;
    @FXML
    private CheckComboBox<String> linesComboBox;
    @FXML
    private TextField messageTextField;
    private ObservableList<String> linesNames;
    private ObservableList<Integer> linesSelected;
    private Database database;
    SynchronizedArrayList<Notification> notificationsList;

    /**
     * Listener adicionado ao SynchronizedArrayList permitindo adicionar novos elementos e atualizar a lista de forma autónoma
     *
     * @return ListChangeListener a ser adicionado ao SynchronizedArrayList
     */
    private ListChangeListener<Notification> getListChangeListener() {
        return c -> Platform.runLater(
                () -> {
                    if (c.next()) {
                        if (c.wasAdded()) {
                            for (Notification notification : c.getAddedSubList()) {
                                content.getChildren().add(createNotification(notification.getNotificationDate(), notification.getLine(), notification.getUserEmail(), notification.getMessage()));
                            }
                        } else {
                            content.getChildren().clear();
                            for (Notification notification : c.getList()) {
                                content.getChildren().add(createNotification(notification.getNotificationDate(), notification.getLine(), notification.getUserEmail(), notification.getMessage()));
                            }
                        }
                    }
                }
        );
    }

    public void run(SynchronizedArrayList<Notification> notificationsList) {
        this.notificationsList = notificationsList;
        database = new Database(notificationsList);
        linesNames = linesComboBox.getItems();
        notificationsList.addListener(getListChangeListener());

        for (Line linhaFerroviaria : database.getLinesList()) {
            if (!linesNames.contains(linhaFerroviaria.getName())) {
                linesNames.add(linhaFerroviaria.getName());
            }
        }
        linesComboBox.setTitle(lines);

        linesSelected = linesComboBox.getCheckModel().getCheckedIndices();

        linesSelected.addListener((ListChangeListener<Integer>) c -> {
            if (linesComboBox.getCheckModel().isEmpty()) {
                linesComboBox.setTitle(lines);
            } else {
                linesComboBox.setTitle(null);
            }
        });
    }

    /**
     * Função utilizada para adicionar uma notificação(linha) no ecrã
     *
     * @param notificationDate data da notificação
     * @param lineName         nome da linha
     * @param email            email do utilizador que enviou a notificação
     * @param message          mensagem da notificação
     * @return
     */
    private HBox createNotification(String notificationDate, String lineName, String email, String message) {
        HBox hBox = new HBox();
        hBox.setSpacing(20);
        if (lineName != null) {
            hBox.getChildren().addAll(
                    new Text(notificationDate), new Text(lineName), new Text(email), new Text(message)
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
    protected void OnClickSend(ActionEvent event) {
        String notificationMessage = messageTextField.getText();
        for (Integer index : linesSelected) {
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

            Notification notification = new Notification(now.format(formatter), "CentralServer", notificationMessage, linesNames.get(index));
            database.insertNotification(notification);
            NotificationSender sender = new NotificationSender(notification);
            messageTextField.setText("");
            sender.start();
        }
    }
}
