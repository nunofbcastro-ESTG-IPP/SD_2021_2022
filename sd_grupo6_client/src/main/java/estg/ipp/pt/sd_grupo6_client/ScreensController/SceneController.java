package estg.ipp.pt.sd_grupo6_client.ScreensController;

import estg.ipp.pt.sd_grupo6_client.Main;
import estg.ipp.pt.sd_grupo6_client.ScreensController.manager.HomeManagerController;
import estg.ipp.pt.sd_grupo6_client.ScreensController.passenger.HomePassengerController;
import estg.ipp.pt.sd_grupo6_client.Utils.SynchronizedArrayList;
import estg.ipp.pt.sd_grupo6_client.models.Notification;
import estg.ipp.pt.sd_grupo6_client.models.User;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class SceneController {
    private static void createScreen(Scene scene, Stage stage, String title) {
        stage.setResizable(false);
        stage.setTitle(title+" - Train Assistant");
        stage.setScene(scene);
        stage.getIcons().add(new Image(Objects.requireNonNull(Main.class.getResourceAsStream("icon.png"))));
        stage.show();
    }

    public static void Authentication(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource(SceneName.authentication));
        Parent root = loader.load();

        AuthenticationController controller = loader.getController();
        controller.run();

        Scene scene = new Scene(root);

        createScreen(scene, stage, "Autenticação");
        stage.setOnCloseRequest(event -> {});
    }

    public static void Home(Stage stage, String userJson) throws IOException {
        Parent root;

        User passenger = User.StringJsonToUser(userJson);

        SynchronizedArrayList<Notification> reportsList = new SynchronizedArrayList<>();
        SynchronizedArrayList<Notification> managerReportList = new SynchronizedArrayList<>();

        if(passenger.getRole().equals("Manager")){
            FXMLLoader loader = new FXMLLoader(Main.class.getResource(SceneName.managerHome));
            root = loader.load();

            HomeManagerController controller = loader.getController();
            controller.run(passenger, managerReportList, stage);

        }else{
            FXMLLoader loader = new FXMLLoader(Main.class.getResource(SceneName.passengerHome));
            root = loader.load();

            HomePassengerController controller = loader.getController();
            controller.run(passenger, reportsList, stage);
        }

        Scene scene = new Scene(root);

        createScreen(scene, stage, "Home");
    }
}
