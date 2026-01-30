package com.nexus.desktop;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

/**
 * JavaFX App for NEXUS Recruitment Platform
 */
public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        scene = new Scene(loadFXML("primary"), 1024, 768);
        stage.setTitle("NEXUS - Recruitment Platform");
        stage.setScene(scene);
        stage.show();
    }

    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader();
        
        // Try loading from classpath - use the correct resource path
        URL resourceUrl = App.class.getClassLoader().getResource(fxml + ".fxml");
        if (resourceUrl == null) {
            throw new IOException("FXML file not found: " + fxml + ".fxml");
        }
        
        fxmlLoader.setLocation(resourceUrl);
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }

}