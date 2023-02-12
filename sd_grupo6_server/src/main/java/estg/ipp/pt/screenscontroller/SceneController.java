package estg.ipp.pt.screenscontroller;

import estg.ipp.pt.Main;
import estg.ipp.pt.models.Notification;
import estg.ipp.pt.screenscontroller.admin.HomeController;
import estg.ipp.pt.utlis.SynchronizedArrayList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class SceneController {
    private static void createScreen(Scene scene, Stage stage, String title) {
        stage.setResizable(false);
        stage.setTitle(title + " - Train Assistant");
        stage.setScene(scene);
        stage.getIcons().add(new Image(Main.class.getResourceAsStream("icon.png")));
        stage.show();
    }

    public static void Home(Stage stage, SynchronizedArrayList<Notification> reportsList) throws IOException {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource(SceneName.home));
        Parent root = loader.load();

        HomeController controller = loader.getController();
        controller.run(reportsList);

        Scene scene = new Scene(root);

        createScreen(scene, stage, "Home");
    }

}
