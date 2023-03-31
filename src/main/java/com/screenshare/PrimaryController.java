package com.screenshare;

import java.awt.List;
import java.io.IOException;
import java.util.EnumSet;

import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Duration;


public class PrimaryController {    
    
    public enum QualityLevel {
        LOW(0.3f), MEDIUM(0.6f), HIGH(0.9f);
        private final float value;
        
        QualityLevel(float value) {
            this.value = value;
        }
        public float getValue() {
            return value;
        }
    }

    public enum Resolution{
        LOW(640, 480), MEDIUM( 1280, 720), HIGH( 1920, 1080);
        private final int width;
        private final int heigth;

        Resolution(int width, int height){
            this.width = width;
            this.heigth = height;
        }
        public int getWidth(){
            return width;
        }
        public int getHeight(){
            return heigth;
        }
    }



    private final ObservableList<String> screenIndexItems = FXCollections.observableArrayList(ScreenShare.getScreenNames());
    private final ObservableList<Resolution> resolutionItems = FXCollections.observableArrayList(EnumSet.allOf(Resolution.class));
    private final ObservableList<QualityLevel> qualityLevelItems = FXCollections.observableArrayList(EnumSet.allOf(QualityLevel.class));

                                                         
    @FXML Label urlLink;
    @FXML ChoiceBox<Resolution> selectResolution;
    @FXML ChoiceBox<QualityLevel> selcectionQuality;
    @FXML ChoiceBox<String> SelectScreenIndex;


    public static Resolution resolution = Resolution.MEDIUM;
    public static QualityLevel qualityLevel = QualityLevel.MEDIUM; 
    public static int screenIndex = 0; 

    @FXML
    private void initialize() {
        //Set Box items
        selectResolution.setItems(resolutionItems); // 640x480, 1280x720, 1920x1080
        selcectionQuality.setItems(qualityLevelItems); // LOW, MEDIUM, HIGH
        SelectScreenIndex.setItems(screenIndexItems);
        
        SelectScreenIndex.setValue(ScreenShare.getScreenNames()[screenIndex]);
        selcectionQuality.setValue(qualityLevel);
        selectResolution.setValue(resolution);

        selcectionQuality.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue != null){
                qualityLevel = newValue;
            }
        });

        selectResolution.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue != null){
                resolution = newValue;
            }
        });

        SelectScreenIndex.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue != null){
                screenIndex = SelectScreenIndex.getSelectionModel().getSelectedIndex();
            }
        });

        try {
            String publicIPAddress = ScreenShare.getPublicIPAddress();
            System.out.println("Public IP Address: " + publicIPAddress);
            String route_url = "http://" + publicIPAddress + ":5500/screen/image";
            urlLink.setText(route_url);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }    


    @FXML
    private void copyToClipboard() {
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        final ClipboardContent content = new ClipboardContent();
        content.putString(urlLink.getText());
        String temp = urlLink.getText();
        urlLink.setText("Copied to clipboard");
        PauseTransition pause = new PauseTransition(Duration.seconds(1));
        pause.setOnFinished(event -> {
            urlLink.setText(temp);
        });
        pause.play();
        clipboard.setContent(content);
    }
    

    


}
