package com.screenshare;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class PrimaryController {
    @FXML Label url;


    // run when initialize
    @FXML
    private void initialize() {
        try {
            String publicIPAddress = getPublicIPAddress();
            System.out.println("Public IP Address: " + publicIPAddress);
            String route_url = "http://" + publicIPAddress + ":5500/screen";
            url.setText(route_url);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getPublicIPAddress() throws IOException {
        URL url = new URL("https://api.ipify.org");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            connection.disconnect();
            return content.toString();
        } else {
            throw new IOException("Failed to get public IP address. HTTP response code: " + responseCode);
        }
    }

    @FXML
    private void switchToSecondary() throws IOException {
    }
}
