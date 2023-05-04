package com.screenshare;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import spark.Spark;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static spark.Spark.get;
import static spark.Spark.port;
import static spark.Spark.awaitInitialization;

import com.google.gson.Gson;

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

        Alert warning = new Alert(Alert.AlertType.WARNING);
        warning.setTitle("Warning");
        warning.setHeaderText("Be careful when using this application");
        warning.setContentText(
                "This application is not secure and do NOT use it to share sensitive information.\nUse it at your own risk.");
        warning.showAndWait();

        // Start the HTTP API server
        startApiServer();
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ScreenShare.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    private void startApiServer() {
        port(5500);

        try {
            init();
        } catch (Exception e) {
            System.out.println("Error: " + e);
        }

        get("/screen", (req, res) -> {
            res.type("application/json"); // Set the response type to JSON

            int screenIndex = PrimaryController.screenIndex;
            int maxWidth = PrimaryController.resolution.getWidth();
            int maxHeight = PrimaryController.resolution.getHeight();

            String[][] pixels = Backend.screenToPixels(screenIndex, maxWidth, maxHeight);
            Gson gson = new Gson();
            String json = gson.toJson(pixels);

            return json;
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

        get("/screen/image/base64", (req, res) -> {
            res.type("text/plain"); // Set the response type to text/plain

            int screenIndex = PrimaryController.screenIndex;
            float compressionLevel = PrimaryController.qualityLevel.getValue();
            int maxWidth = PrimaryController.resolution.getWidth();
            int maxHeight = PrimaryController.resolution.getHeight();

            String image = Backend.captureAndCompressScreenBase64(screenIndex, compressionLevel, maxWidth,
                    maxHeight);

            Map<String, String> result = new HashMap<>();
            result.put("compressionLevel", String.valueOf(compressionLevel));
            result.put("maxWidth", String.valueOf(maxWidth));
            result.put("maxHeight", String.valueOf(maxHeight));
            result.put("imageBase64", image);

            Gson gson = new Gson();
            String json = gson.toJson(result);
            return json;
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
