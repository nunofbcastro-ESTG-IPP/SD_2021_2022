module estg.ipp.pt {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.base;
    requires org.controlsfx.controls;
    requires java.logging;

    requires org.mongodb.driver.core;
    requires org.mongodb.bson;
    requires org.mongodb.driver.sync.client;

    requires com.google.gson;

    requires io.github.cdimascio.dotenv.java;

    requires de.mkammerer.argon2.nolibs;
    requires org.slf4j;

    opens estg.ipp.pt to javafx.fxml;
    exports estg.ipp.pt;
    opens estg.ipp.pt.screenscontroller to javafx.fxml;
    exports estg.ipp.pt.screenscontroller;
    opens estg.ipp.pt.screenscontroller.admin to javafx.fxml;
    exports estg.ipp.pt.screenscontroller.admin;
    exports estg.ipp.pt.models;
}