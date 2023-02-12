package estg.ipp.pt.sd_grupo6_client.ScreensController.passenger;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import estg.ipp.pt.sd_grupo6_client.Utils.SynchronizedArrayList;
import estg.ipp.pt.sd_grupo6_client.localTrafficManager.LocalTrafficServer;
import estg.ipp.pt.sd_grupo6_client.models.Line;
import estg.ipp.pt.sd_grupo6_client.models.User;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

/**
 * Controller que gere a página "Subscrever a novas linhas"
 */
public class SubscribeController {
    @FXML
    private VBox content;

    private User passenger;
    private ArrayList<Socket> socketList;

    private SynchronizedArrayList<Line> syncLines = new SynchronizedArrayList<>();

    /**
     * Listener adicionado ao SynchronizedArrayList permitindo adicionar novos elementos e atualizar a lista de forma autónoma
     *
     * @return ListChangeListener a ser adicionado ao SynchronizedArrayList
     */
    private ListChangeListener<Line> getListChangeListener() {
        return c -> Platform.runLater(
                () -> {
                    if (c.next()) {
                        if (c.wasAdded()) {
                            for (Line line : c.getAddedSubList()) {
                                content.getChildren().add(lineSubscribe(line));
                            }
                        } else {
                            content.getChildren().clear();
                            for (Line line : c.getList()) {
                                content.getChildren().add(lineSubscribe(line));
                            }
                        }
                    }
                }
        );
    }

    public void run(User passenger, ArrayList<Socket> socketList) throws IOException {
        content.getChildren().clear();

        syncLines = new SynchronizedArrayList<>();
        this.syncLines.addListener(getListChangeListener());
        this.syncLines.clearList();

        ArrayList<Line> lines;

        LocalTrafficServer localTrafficServer = new LocalTrafficServer();
        String linesJson = localTrafficServer.getAllLines();

        this.passenger = passenger;
        this.socketList = socketList;
        Gson g = new Gson();
        lines = Line.StringJsonToLines(g.fromJson(linesJson, JsonArray.class));

        for (Line line : lines) {
            for (String passengerLine : passenger.getLines()) {
                if (Objects.equals(line.getNome(), passengerLine)) {
                    line.setSubscribed(true);
                }
            }

            this.syncLines.add(line);
        }
    }

    /**
     * Função responsável por alterar o conteúdo de um determinado botão
     *
     * @param button       botão a ser alterado
     * @param isSubscribed estado utilizado para identificar o estilo do botão
     */
    private void setStyleButton(Button button, Boolean isSubscribed) {
        button.getStyleClass().removeAll(button.getStyleClass());
        if (isSubscribed) {
            button.setText("Cancelar subscrição");
            button.getStyleClass().add("button");
            button.getStyleClass().add("button3");
        } else {
            button.setText("Subscrever");
            button.getStyleClass().add("button");
            button.getStyleClass().add("button2");
        }
    }

    /**
     * Cria uma linha no ecrã com um botão para subscrever/cancelar subscrição
     *
     * @param line linha a ser adicionada
     */
    public HBox lineSubscribe(Line line) {
        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_LEFT);

        hBox.setSpacing(20);

        Button button = new Button();
        setStyleButton(button, line.getSubscribed());

        button.setOnAction(value -> {
            line.setSubscribed(!line.getSubscribed());
            setStyleButton(button, line.getSubscribed());
            String lineName = null;
            for (String passengerLine : passenger.getLines()) {
                if (Objects.equals(line.getNome(), passengerLine)) {
                    lineName = passengerLine;
                    break;
                }
            }

            if (lineName != null) {
                passenger.removeLine(lineName);

                LocalTrafficServer localTrafficServer = new LocalTrafficServer();
                String jsonLine = localTrafficServer.getLine(lineName);
                Gson g = new Gson();
                JsonObject jo = g.fromJson(jsonLine, JsonObject.class);

                boolean removed = false;
                for (int i = 0; i < socketList.size(); i++) {
                    if (socketList.get(i) != null && socketList.get(i).getLocalPort() == Integer.parseInt(jo.get("Port").toString())) {
                        socketList.remove(i);
                        removed = true;
                        break;
                    }
                }

                if (!removed) {
                    for (int i = 0; i < socketList.size(); i++) {
                        if (socketList.get(i) == null) {
                            socketList.remove(i);
                            break;
                        }
                    }
                }

                String result = updateLine("RemoveLine", passenger.getEmail(), line.getNome());
                if (result != null) {
                    System.out.println("Linha removida com sucesso!");
                } else {
                    System.out.println("Ocorreu um problema ao remover a linha");
                }
            } else {
                passenger.addLine(line.getNome());
                socketList.add(null);
                String result = updateLine("AddLine", passenger.getEmail(), line.getNome());
                if (result != null) {
                    System.out.println("Linha adicionada com sucesso!");
                } else {
                    System.out.println("Ocorreu um problema ao adicionar a linha");
                }
            }
        });


        hBox.getChildren().addAll(
                new Text(line.getNome()), button
        );
        return hBox;
    }

    /**
     * Subscreve ou cancela a subscrição a uma linha tendo em conta a tag recebida
     *
     * @param tag      tag que identifica se está a subscrever ou a cancelar subscrição
     * @param email    email do passageiro a ser afetado
     * @param lineName nome da linha a ser afetada
     * @return Linha alterada ou null em caso de erro
     */
    public String updateLine(String tag, String email, String lineName) {
        Socket s;
        PrintWriter out;

        try {
            s = new Socket("127.0.0.1", 2050);
            out = new PrintWriter(s.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            out.println(Arrays.toString(new String[]{tag, email, lineName}));
            String input;

            if ((input = in.readLine()) != null) {
                out.close();
                in.close();
                s.close();

                return input;
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
            System.exit(-1);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-2);
        }

        return null;
    }
}
