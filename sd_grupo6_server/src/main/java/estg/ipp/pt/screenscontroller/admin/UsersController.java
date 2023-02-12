package estg.ipp.pt.screenscontroller.admin;

import estg.ipp.pt.models.Line;
import estg.ipp.pt.database.Database;
import estg.ipp.pt.models.User;
import estg.ipp.pt.screenscontroller.MyNotifications;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.fxml.FXML;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Controller que gere a página "Gerir utilizadores"
 */
public class UsersController {
    private static final String demoteManager = "Despromover gestor";
    private static final String promoteManager = "Promover gestor";
    private static final String noLines = "Sem linha";
    @FXML
    private VBox content;
    private ObservableList<String> oliLines;

    private final Database database = new Database();

    public void run() {
        content.getChildren().clear();

        oliLines = FXCollections.observableList(new ArrayList<>());

        oliLines.add(noLines);
        for (Line line : database.getLinesList()) {
            oliLines.add(line.getName());
        }

        for (User passenger : database.getUsersList()) {
            createLineUser(passenger);
        }
    }

    /**
     * Função responsável por alterar o conteúdo de um determinado botão
     *
     * @param button    botão a ser alterado
     * @param combo_box combo box adicionada caso o utilizador seja um gestor local
     * @param lines     linhas a que o utilizador está associado
     * @param isManager variável true caso o utilizador seja gestor local, caso contrário, false
     */
    private void setStyleButton(Button button, ComboBox combo_box, List<String> lines, Boolean isManager) {
        button.getStyleClass().removeAll(button.getStyleClass());
        if (isManager) {
            if (!lines.isEmpty()) {
                changeComboxValue(combo_box, lines.get(0));
            } else {
                changeComboxValue(combo_box, noLines);
            }
            button.setText(demoteManager);
            button.getStyleClass().add("button");
            button.getStyleClass().add("button4");
            combo_box.setVisible(true);
        } else {
            changeComboxValue(combo_box, noLines);
            button.setText(promoteManager);
            button.getStyleClass().add("button");
            button.getStyleClass().add("button1");
            combo_box.setVisible(false);
        }
    }

    /**
     * Função responsável por alterar o valor da combo box
     *
     * @param comboBox combo box a ser alterada
     * @param value    valor a ser inserido
     */
    private void changeComboxValue(ComboBox comboBox, Object value) {
        EventHandler<ActionEvent> filter = Event::consume;
        comboBox.addEventFilter(ActionEvent.ACTION, filter);
        comboBox.setValue(value);
        comboBox.removeEventFilter(ActionEvent.ACTION, filter);
    }

    /**
     * Função utilizada para adicionar uma nova linha ao ecrã com dados de um utilizador
     *
     * @param user utilizador a ser adicionado no ecrã
     */
    private void createLineUser(User user) {
        boolean isManager = user.getRole().equals("Manager");

        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.setSpacing(20);

        ComboBox combo_box = new ComboBox(oliLines);
        combo_box.setValue(noLines);
        combo_box.setOnAction(value -> {
            boolean isNotError;
            String lineSelected = combo_box.getValue().toString();
            if (lineSelected.equals(noLines)) {
                isNotError = database.removeUserLines(user.getEmail());
            } else {
                isNotError = database.changeManagerLine(user.getEmail(), lineSelected);
            }

            if (isNotError) {
                MyNotifications.showSuccess("Linha alterada com sucesso");
                List<String> list = new ArrayList<>();
                list.add(lineSelected);
                user.setLines(list);
            } else {
                System.out.println(user.getLines().isEmpty());
                if (!user.getLines().isEmpty()) {
                    changeComboxValue(combo_box, user.getLines().get(0));
                } else {
                    changeComboxValue(combo_box, noLines);
                }

                MyNotifications.showError("A linha " + lineSelected + " já contem um gestor.");
            }
        });

        Button button = new Button();
        setStyleButton(button, combo_box, user.getLines(), isManager);
        button.setOnAction(value -> {
            boolean isActive = Objects.equals(button.getText(), promoteManager);

            setStyleButton(button, combo_box, user.getLines(), isActive);

            boolean isNotError = database.changeRoleUser(user.getEmail(), isActive);

            if (isNotError) {
                user.setLines(new ArrayList<>());
                MyNotifications.showSuccess("Cargo alterado com sucesso");
            } else {
                MyNotifications.showSuccess("Erro ao alterar cargo");
            }

        });

        hBox.getChildren().addAll(
                new Text(user.getEmail()), button, combo_box
        );

        content.getChildren().add(hBox);
    }
}
