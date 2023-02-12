package estg.ipp.pt.sd_grupo6_client.ScreensController.passenger;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import estg.ipp.pt.sd_grupo6_client.Utils.SynchronizedArrayList;
import estg.ipp.pt.sd_grupo6_client.localTrafficManager.LocalTrafficServer;
import estg.ipp.pt.sd_grupo6_client.models.User;
import estg.ipp.pt.sd_grupo6_client.models.Notification;
import estg.ipp.pt.sd_grupo6_client.passenger.PassengerTcp;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Controller que gere a página "Ver linhas"
 */
public class LinesPassengerController {
    @FXML
    private ComboBox<String> selectBox;
    @FXML
    private ComboBox<String> selectBoxMessage;
    @FXML
    private TextField messageTextField;
    @FXML
    private Button buttonSendMessage;
    @FXML
    private VBox content;

    private static String notifications = "Novas notificações";

    private ArrayList<Socket> socketList;
    User passenger;
    ObservableList<String> data = FXCollections.observableArrayList();
    ObservableList<String> dataMessage = FXCollections.observableArrayList();
    PassengerTcp passengerTcp = new PassengerTcp();
    SynchronizedArrayList<Notification> notificationsList = new SynchronizedArrayList<>();

    /**
     * Listener adicionado à SynchronizedArrayList permitindo adicionar novos elementos e atualizar a lista de forma autónoma
     * @return ListChangeListener a ser adicionado à SynchronizedArrayList
     */
    private ListChangeListener<Notification> getListChangeListener() {
        return c -> Platform.runLater(
                () -> {
                    if (c.next()) {
                        if (c.wasAdded()) {
                            for (Notification notification : c.getAddedSubList()) {
                                System.out.println(notification);
                                content.getChildren().add(createNotification(notification));
                            }
                        } else {
                            content.getChildren().clear();
                            for (Notification notification : c.getList()) {
                                content.getChildren().add(createNotification(notification));
                            }
                        }
                    }
                }
        );
    }

    /**
     * Ao carregar a página é necessário verificar quais são as linhas a que o passageiro
     * está subscrito e associa as linhas às combo box.
     * @param passenger passageiro conectado
     * @param socketList lista de conexões com gestores locais
     * @param notificationsList lista onde serão adicionadas novas notificações
     */
    public void run(User passenger, ArrayList<Socket> socketList, SynchronizedArrayList<Notification> notificationsList) {
        this.dataMessage.clear();
        this.data.clear();

        this.selectBox.setDisable(false);
        this.selectBoxMessage.setDisable(false);
        this.messageTextField.setDisable(false);
        this.buttonSendMessage.setDisable(false);

        this.data.add(notifications);
        changeComboBoxValue(selectBox, notifications);
        selectBoxMessage.setDisable(false);

        if (socketList == null) {
            disableContent("Não existe nenhum gestor local ativo de momento para as linhas a que está subscrito");
        } else {
            this.notificationsList = notificationsList;
            this.notificationsList.addListener(getListChangeListener());

            if (!passenger.getLines().isEmpty()) {
                LocalTrafficServer localTrafficServer = new LocalTrafficServer();
                Gson g = new Gson();
                String jsonLine;
                JsonObject jo;
                //percorrer a lista de sockets e verificar as linhas que contem sockets != null para apresentar nas select boxes
                for (int i = 0; i < passenger.getLines().size(); i++) {
                    jsonLine = localTrafficServer.getLine(passenger.getLines().get(i));
                    jo = g.fromJson(jsonLine, JsonObject.class);

                    if (Boolean.parseBoolean(jo.get("IsActive").toString())) {
                        String host = jo.get("Host").getAsString();
                        int port = Integer.parseInt(jo.get("Port").toString());

                        for (Socket s : socketList) {
                            if (s != null && Objects.equals(s.getInetAddress().toString().substring(1), host) && s.getPort() == port) {
                                if (!data.contains(passenger.getLines().get(i))) {
                                    data.add(passenger.getLines().get(i));
                                }
                                if (!dataMessage.contains(passenger.getLines().get(i))) {
                                    dataMessage.add(passenger.getLines().get(i));
                                }
                            }
                        }
                    }
                }
                this.passenger = passenger;
                this.socketList = socketList;

                passengerTcp.receiveNotification(notificationsList, socketList);

                selectBox.setItems(
                        data
                );

                selectBoxMessage.setItems(
                        dataMessage
                );
            } else {
                disableContent("Por favor subscreva a pelo menos uma linha antes de utilizar esta funcionalidade");
            }
        }
    }

    /**
     * Altera o valor da combo box
     * @param comboBox combo box a ser alterada
     * @param value novo valor
     */
    private void changeComboBoxValue(ComboBox comboBox, Object value) {
        EventHandler<ActionEvent> filter = Event::consume;
        comboBox.addEventFilter(ActionEvent.ACTION, filter);
        comboBox.setValue(value);
        comboBox.removeEventFilter(ActionEvent.ACTION, filter);
    }

    /**
     * Desativa todos os inputs da página (textFields e combo box)
     * @param message mensagem a mostrar ao utilizador
     */
    private void disableContent(String message) {
        content.getChildren().add(new Text(message));
        selectBox.setDisable(true);
        selectBoxMessage.setDisable(true);
        messageTextField.setDisable(true);
        buttonSendMessage.setDisable(true);
    }

    /**
     * Cria uma notificação no ecrã
     * @param notification notificação a ser adicionada
     */
    private VBox createNotification(Notification notification) {
        VBox vBox = new VBox();
        vBox.setSpacing(5);

        HBox hBox1 = new HBox();
        hBox1.setSpacing(20);
        hBox1.getChildren().addAll(
                new Text(notification.getUserEmail())
        );

        HBox hBox2 = new HBox();
        hBox2.setSpacing(20);
        hBox2.getChildren().addAll(
                new Text(notification.getNotificationDateTime()), new Text(notification.getLine()), new Text(notification.getMessage())
        );

        vBox.getChildren().addAll(
                hBox1, hBox2
        );

        return vBox;
    }

    /**
     * Interação com a combo box inicial.
     * Por default mostra as notificações em tempo real, mas ao selecionar uma linha, mostra
     * o histórico da mesma.
     * @param event evento da combo box
     */
    @FXML
    private void changeLine(ActionEvent event) {
        notificationsList.clearList();

        if (!notifications.equals(selectBox.getValue())) {
            selectBoxMessage.setDisable(true);
            changeComboBoxValue(selectBoxMessage, selectBox.getValue());

            String notifications = passengerTcp.getReportsByLineFromServer(selectBox.getValue());

            Gson g = new Gson();
            JsonArray notificationsArray = g.fromJson(notifications, JsonArray.class);

            for (int i = 0; i < notificationsArray.size(); i++) {
                JsonObject notificationObject = g.fromJson(notificationsArray.get(i), JsonObject.class);
                Notification notification = new Notification(
                        notificationObject.get("NotificationDate").getAsString(),
                        notificationObject.get("UserEmail").getAsString(),
                        notificationObject.get("Message").getAsString(),
                        notificationObject.get("Line").getAsString()
                );

                notificationsList.add(notification);
            }
        } else {
            selectBoxMessage.setDisable(false);
        }
    }

    /**
     * Interação com o botão "Enviar"
     * @param event evento do botão
     */
    @FXML
    private void onSendClick(ActionEvent event) {
        String notificationMessage = messageTextField.getText();
        String selectedLine = selectBoxMessage.getValue();

        if (notificationMessage.length() == 0 || selectedLine.length() == 0) {
            return;
        }

        LocalTrafficServer localTrafficServer = new LocalTrafficServer();
        String jsonLine = localTrafficServer.getLine(selectedLine);
        Gson g = new Gson();
        JsonObject jo = g.fromJson(jsonLine, JsonObject.class);

        //percorrer a lista de sockets para encontrar a linha selecionada e ver se é possivel enviar algo
        for (Socket s : socketList) {
            if (s != null && s.getPort() == Integer.parseInt(jo.get("Port").toString())) {
                LocalDateTime now = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

                String passengerEmailAndRole = passenger.getEmail() + " (" + passenger.getRole() + ")";

                Notification notification = new Notification(now.format(formatter), passengerEmailAndRole , notificationMessage, selectedLine);
                passengerTcp.sendNotification(notification, s);
                messageTextField.setText("");
            }
        }
    }
}
