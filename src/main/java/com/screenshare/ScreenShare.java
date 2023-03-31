package com.screenshare;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import spark.Spark;

import java.io.IOException;
import static spark.Spark.get;
import static spark.Spark.port;
import static spark.Spark.awaitInitialization;

public class ScreenShare extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        scene = new Scene(loadFXML("main"), 400, 300);
        stage.setScene(scene);
        stage.show();
        stage.setTitle("Screen Share");
        stage.getIcons().add(new javafx.scene.image.Image(ScreenShare.class.getResource("icon.png").toExternalForm()));
        stage.setResizable(false);
        // Start the HTTP API server
        startApiServer();
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ScreenShare.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    private void startApiServer() {
        port(5500);

        try{
            init();
        }
        catch(Exception e){
            System.out.println("Error: " + e);
        }

        get("/screen", (req, res) -> {
            res.type("text/plain"); // Set the response type to text/plain
            
            int screenIndex = PrimaryController.screenIndex;
            float compressionLevel = PrimaryController.qualityLevel.getValue();
            int maxWidth = PrimaryController.resolution.getWidth();
            int maxHeight = PrimaryController.resolution.getHeight();

            String base64Image = Backend.captureAndCompressScreenBase64(screenIndex, compressionLevel, maxWidth, maxHeight);
            return base64Image;
        });

        get("/screen/image", (req, res) -> {
            res.type("image/jpeg"); // Set the response type to image/jpeg

            int screenIndex = PrimaryController.screenIndex;
            float compressionLevel = PrimaryController.qualityLevel.getValue();
            int maxWidth = PrimaryController.resolution.getWidth();
            int maxHeight = PrimaryController.resolution.getHeight();

            byte[] image = Backend.captureAndCompressScreenImage(screenIndex, compressionLevel, maxWidth, maxHeight);
            return image;

        });

        // Update the UI with the API server status
        awaitInitialization();
    }

    @Override
    public void stop() {
        stopApiServer();
    }

    private void stopApiServer() {
        Spark.stop();
    }
}
