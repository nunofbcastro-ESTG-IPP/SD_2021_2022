package estg.ipp.pt.sd_grupo6_client.ScreensController;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import estg.ipp.pt.sd_grupo6_client.Utils.Validations;
import estg.ipp.pt.sd_grupo6_client.localTrafficManager.LocalTrafficServer;
import estg.ipp.pt.sd_grupo6_client.models.Line;
import estg.ipp.pt.sd_grupo6_client.models.User;
import estg.ipp.pt.sd_grupo6_client.passenger.PassengerTcp;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.controlsfx.control.CheckComboBox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Controller que gere as páginas de autenticação (login e registo)
 */
public class AuthenticationController {
    private final String lines = "Linhas";

    @FXML
    private StackPane Content;
    @FXML
    private VBox SignIn;
    @FXML
    private TextField SignInEmail;
    @FXML
    private PasswordField SignInPassword;
    @FXML
    private Label SignInError;

    @FXML
    private ScrollPane SignUp;
    @FXML
    private TextField SignUpUsername;
    @FXML
    private TextField SignUpEmail;
    @FXML
    private PasswordField SignUpPassword;
    @FXML
    private PasswordField SignUpPasswordConfirmation;
    @FXML
    private Label SignUpError;
    @FXML
    private CheckComboBox<String> linesComboBox;
    private VBox ProgressIndicatorVBox;

    private ObservableList<String> linesNames;
    private ObservableList<Integer> linesSelected;

    public void run() {
        linesNames = linesComboBox.getItems();
        linesSelected = linesComboBox.getCheckModel().getCheckedIndices();

        linesComboBox.setTitle(lines);

        linesSelected = linesComboBox.getCheckModel().getCheckedIndices();

        linesSelected.addListener((ListChangeListener<Integer>) c -> {
            if (linesComboBox.getCheckModel().isEmpty()) {
                linesComboBox.setTitle(lines);
            } else {
                linesComboBox.setTitle(null);
            }
        });

        ProgressIndicator pi = new ProgressIndicator();
        ProgressIndicatorVBox = new VBox(pi);
        ProgressIndicatorVBox.setAlignment(Pos.CENTER);

        LocalTrafficServer localTrafficServer = new LocalTrafficServer();
        String linesJson = localTrafficServer.getAllLines();

        Gson g = new Gson();
        List<Line> lines = Line.StringJsonToLines(g.fromJson(linesJson, JsonArray.class));

        for (Line line: lines) {
            linesNames.add(line.getNome());
        }


    }

    /**
     * Adiciona o loading ao ecrã
     */
    public <T extends Region> void setLoading(T box) {
        Platform.runLater(() -> {
            // Grey Background
            box.setDisable(true);
            Content.getChildren().add(ProgressIndicatorVBox);
        });
    }

    /**
     * Remove o loading do ecrã
     */
    public <T extends Region> void setNotLoading(T box) {
        Platform.runLater(() -> {
            // Grey Background
            box.setDisable(false);
            Content.getChildren().remove(ProgressIndicatorVBox);
        });
    }

    /**
     * Limpa os dados de todos os inputs do login e registo
     */
    private void clear() {
        SignInEmail.setText("");
        SignInPassword.setText("");
        SignInError.setVisible(false);

        SignUpUsername.setText("");
        SignUpPassword.setText("");
        SignUpPasswordConfirmation.setText("");
        SignInError.setVisible(false);
    }

    /**
     * Interação com o botão "Não tem uma conta? Registar-se".
     * Ao clicar no botão é redirecionado para a página de registo.
     */
    @FXML
    protected void onSignInSignUpClick() {
        clear();

        SignIn.setVisible(false);
        SignUp.setVisible(true);
    }

    /**
     * Interação com o botão "Já tem uma conta? Logar-se".
     * Ao clicar no botão é redirecionado para a página de login.
     */
    @FXML
    protected void onSignUpSignInClick() {
        clear();

        SignIn.setVisible(true);
        SignUp.setVisible(false);
    }

    /**
     * Interação com o botão "Entrar".
     * Verifica os dados inseridos no login.
     * Se algo estiver errado mostra um erro, caso contrário faz login.
     * @param event evento do botão
     */
    @FXML
    protected void onSignInClick(ActionEvent event) {
        setLoading(SignIn);

        SignInError.setVisible(false);

        String username = SignInEmail.getText();
        String password = SignInPassword.getText();

        if (username.length() < 3) {
            SignInError.setText("Nome de utilizador inválido");
            SignInError.setVisible(true);
            setNotLoading(SignIn);
            return;
        }

        if (!Validations.passwordValidation(password)) {
            SignInError.setText("Password inválida");
            SignInError.setVisible(true);
            setNotLoading(SignIn);
            return;
        }

        try {
            String[] loginData = {username, password};
            PassengerTcp passengerTcp = new PassengerTcp();
            String passenger = passengerTcp.connectWithServer(loginData);

            if (passenger != null) {
                SceneController.Home((Stage) ((Node) event.getSource()).getScene().getWindow(), passenger);
            } else {
                SignInError.setText("Utilizador não encontrado");
                SignInError.setVisible(true);
                setNotLoading(SignIn);
            }
        } catch (IOException ignored) {
        }
    }

    /**
     * Interação com o botão "Registar".
     * Verifica os dados inseridos no registo.
     * Se algo estiver errado mostra um erro, caso contrário regista o utilizador e faz login automaticamente.
     * @param event evento do botão
     */
    @FXML
    protected void onSignUpClick(ActionEvent event) {
        setLoading(SignUp);

        SignUpError.setVisible(false);

        String username = SignUpUsername.getText();
        String email = SignUpEmail.getText();
        String password = SignUpPassword.getText();
        String passwordConfirmation = SignUpPasswordConfirmation.getText();

        if (!Validations.nameValidation(username)) {
            SignUpError.setText("Nome de utilizador inválido");
            SignUpError.setVisible(true);
            setNotLoading(SignUp);
            return;
        }

        if (!Validations.emailValidation(email)) {
            SignUpError.setText("Email inválido");
            SignUpError.setVisible(true);
            setNotLoading(SignUp);
            return;
        }

        if (!Validations.passwordValidation(password)) {
            SignUpError.setText("Password inválida");
            SignUpError.setVisible(true);
            setNotLoading(SignUp);
            return;
        }

        if (!Objects.equals(password, passwordConfirmation)) {
            SignUpError.setText("As passwords são diferentes");
            SignUpError.setVisible(true);
            setNotLoading(SignUp);
            return;
        }

        try {
            List<String> linesList = new ArrayList<>();

            for (Integer index: linesSelected) {
                linesList.add(linesNames.get(index));
            }

            User user = new User(username, email, null, password, linesList);
            PassengerTcp passengerTcp = new PassengerTcp();
            String input = passengerTcp.connectWithServer(user);

            if (input != null) {
                SceneController.Home((Stage) ((Node) event.getSource()).getScene().getWindow(), input);
            } else {
                SignUpError.setText("Não foi possível registar a sua conta");
                SignUpError.setVisible(true);
                setNotLoading(SignUp);
            }
        } catch (IOException ignored) {
        }
    }
}