package estg.ipp.pt.sd_grupo6_client;

import estg.ipp.pt.sd_grupo6_client.ScreensController.SceneController;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        SceneController.Authentication(stage);
    }
    public static void main(String[] args) {
        launch();
    }
}
