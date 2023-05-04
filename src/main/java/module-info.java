module com.screenshare {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires spark.core;
    requires javax.servlet.api; // Add this line
    requires com.google.gson;

    opens com.screenshare to javafx.fxml;

    exports com.screenshare;
}
