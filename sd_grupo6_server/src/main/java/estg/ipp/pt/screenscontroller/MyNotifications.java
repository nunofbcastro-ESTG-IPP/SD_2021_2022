package estg.ipp.pt.screenscontroller;

import estg.ipp.pt.Main;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;

public class MyNotifications {
    private static Duration duration = Duration.seconds(5);
    public static void showError(String text){
        Platform.runLater(() -> Notifications.create()
                .text(text)
                .hideAfter(duration)
                .showError());
    }

    public static void showWarning(String text){
        Platform.runLater(() -> Notifications.create()
                .text(text)
                .hideAfter(duration)
                .showWarning());
    }

    public static void showSuccess(String text){
        Image image = new Image(Main.class.getResourceAsStream("sucesso.png"), 40, 40, true, false);
        ImageView imageView = new ImageView(image);

        Platform.runLater(() -> Notifications.create()
                .text(text)
                .graphic(imageView)
                .hideAfter(duration)
                .show());
    }

    public static void showInfo(String text){
        Platform.runLater(() -> Notifications.create()
                .text(text)
                .hideAfter(duration)
                .showInformation());
    }

    public static void show(String text){
        Platform.runLater(() -> Notifications.create()
                .text(text)
                .hideAfter(duration)
                .show());
    }
}
