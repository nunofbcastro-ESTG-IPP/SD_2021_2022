package estg.ipp.pt.screenscontroller.admin;

import estg.ipp.pt.models.Line;
import estg.ipp.pt.database.Database;
import estg.ipp.pt.screenscontroller.MyNotifications;
import estg.ipp.pt.utlis.Validations;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.fxml.FXML;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Controller que gere a página "Gerir Linhas"
 */
public class LinesController {
    private static final String openLine = "Abrir linha";
    private static final String closeLine = "Fechar linha";
    private static final String invalidLine = "Nome da linha inválida";
    private static final String insertedLine = "Linha inserida com sucesso";
    private static final String alreadyExistsLine = "Linha já existe";

    @FXML
    private VBox content;
    @FXML
    private TextField messageTextField;

    private final List<Line> list = new ArrayList<>();
    private final ObservableList<Line> oli = FXCollections.observableList(list);

    private final Database database = new Database();

    public LinesController() {
        oli.addListener(getListChangeListener());
    }

    /**
     * Listener adicionado à ObservableList permitindo adicionar novos elementos e atualizar a lista de forma autónoma
     * @return ListChangeListener a ser adicionado à ObservableList
     */
    private ListChangeListener<Line> getListChangeListener() {
        return c -> Platform.runLater(() -> {
            if (c.next()) {
                if (c.wasAdded()) {
                    for (Line line : c.getAddedSubList()) {
                        createLine(line);
                    }
                } else {
                    content.getChildren().clear();
                    for (Line line : c.getAddedSubList()) {
                        createLine(line);
                    }
                }
            }
        });
    }

    public void run() {
        List<Line> lines = database.getLinesList();
        for (Line line : lines) {
            if (!oli.contains(line)) {
                oli.add(line);
            }
        }
    }

    /**
     * Função responsável por alterar o conteúdo de um determinado botão
     * @param button botão a ser alterado
     * @param isActive estado utilizado para identificar o estilo do botão
     * @param activeMensage mensagem a inserir no botão caso o estado seja ativo
     * @param notActiveMensage mensagem a inserir no botão caso o estado seja inativo
     */
    private void setStyleButton(Button button, Boolean isActive, String activeMensage, String notActiveMensage) {
        button.getStyleClass().removeAll(button.getStyleClass());
        if (isActive) {
            button.setText(notActiveMensage);
            button.getStyleClass().add("button");
            button.getStyleClass().add("button4");
        } else {
            button.setText(activeMensage);
            button.getStyleClass().add("button");
            button.getStyleClass().add("button1");
        }
    }

    /**
     * Função utilizada para criar uma linha no ecrã
     * @param line linha a ser adicionada
     */
    private void createLine(Line line) {
        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.setSpacing(20);

        Button button = new Button();
        setStyleButton(button, line.getIsActive(), openLine, closeLine);
        button.setOnAction(value -> {
            boolean isActive1 = Objects.equals(button.getText(), openLine);
            setStyleButton(button, isActive1, openLine, closeLine);
            String response = database.changeLineStatus(line.getName(), isActive1);
            if (response == null) {
                MyNotifications.showError("Erro");
            }
        });

        hBox.getChildren().addAll(new Text(line.getName()), button);
        content.getChildren().add(hBox);
    }

    /**
     * Interação com o botão "Criar Linha"
     * @param event evento do botão
     */
    @FXML
    private void OnCreateLine(ActionEvent event) {
        String lineName = messageTextField.getText();

        if (!Validations.lineNameValidation(lineName)) {
            MyNotifications.showWarning(invalidLine);
            return;
        }

        boolean isInserted = database.insertLines(lineName);

        if (isInserted) {
            messageTextField.setText("");
            MyNotifications.showSuccess(insertedLine);
            oli.add(new Line(
                    lineName,
                    false,
                    null,
                    null
            ));
            return;
        }

        MyNotifications.showError(alreadyExistsLine);
    }

    /**
     * Função responsável por alterar o estado de todas as linhas
     * @param isActive estado que será alterado em todas as linhas
     */
    private void changeAllLine(boolean isActive) {
        boolean response = database.changeAllLineStatus(isActive);

        if (response) {
            List<Line> linhasFerroviarias = new ArrayList<>();
            for (Line linhaFerroviaria : oli) {
                linhaFerroviaria.setIsActive(isActive);
                linhasFerroviarias.add(linhaFerroviaria);
            }

            oli.clear();
            oli.addAll(linhasFerroviarias);
        }
    }

    /**
     * Interação com o botão "Fechar linhas"
     * @param event evento do botão
     */
    @FXML
    protected void onClickCloseLines(ActionEvent event) {
        changeAllLine(false);
    }

    /**
     * Interação com o botão "Abrir linhas"
     * @param event evento do botão
     */
    @FXML
    protected void onClickOpenLines(ActionEvent event) {
        changeAllLine(true);
    }
}
