module estg.ipp.pt.sd_grupo6_client {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.controlsfx.controls;

    requires com.google.gson;

    opens estg.ipp.pt.sd_grupo6_client to javafx.fxml;
    exports estg.ipp.pt.sd_grupo6_client;
    exports estg.ipp.pt.sd_grupo6_client.ScreensController.passenger;
    opens estg.ipp.pt.sd_grupo6_client.ScreensController.passenger to javafx.fxml;
    exports estg.ipp.pt.sd_grupo6_client.ScreensController.manager;
    opens estg.ipp.pt.sd_grupo6_client.ScreensController.manager to javafx.fxml;
    exports estg.ipp.pt.sd_grupo6_client.ScreensController;
    opens estg.ipp.pt.sd_grupo6_client.ScreensController to javafx.fxml;
}