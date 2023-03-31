package com.screenshare;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import static spark.Spark.get;
import static spark.Spark.port;
import static spark.Spark.awaitInitialization;


public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        scene = new Scene(loadFXML("main"), 300, 300);
        stage.setScene(scene);
        stage.show();
        stage.setTitle("Screen Share");
        stage.setResizable(false);
        // Start the HTTP API server
        startApiServer();
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    private void startApiServer() {
        // Start the Spark Java HTTP API server
        port(5500);
        try{
            init();
        }
        catch(Exception e){
            System.out.println("Error: " + e);
        }

        /*
         *  Route: /screen
         *  Description: Returns the screen capture as a base64 encoded string
         *  Response Type: text/plain
         */
        get("/screen", (req, res) -> {
            float jpegQuality = 0.5f; // Adjust the quality (0 to 1)
            int maxWidth = 800; // Adjust the maximum width
            int maxHeight = 600; // Adjust the maximum height
            res.type("text/plain"); // Set the response type to text/plain
            String base64Image = ScreenShare.captureAndCompressScreenBase64(jpegQuality, maxWidth, maxHeight);
            return base64Image;
        });

        /*
         *  Route: /screen_image
         *  Description: Returns the screen capture as a byte array
         *  Response Type: image/jpeg
         */
        get("/screen_image", (req, res) -> {
            float jpegQuality = 0.5f; // Adjust the quality (0 to 1)
            int maxWidth = 800; // Adjust the maximum width
            int maxHeight = 600; // Adjust the maximum height
            res.type("image/jpeg"); // Set the response type to image/jpeg
            byte[] image = ScreenShare.captureAndCompressScreenImage(jpegQuality, maxWidth, maxHeight);
            return image;
        });

        // Update the UI with the API server status
        awaitInitialization();
    }

    @Override
    public void stop() {
        // Stop the Spark Java HTTP API server when the JavaFX application is closed
        stopApiServer();
    }

    private void stopApiServer() {
        stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
