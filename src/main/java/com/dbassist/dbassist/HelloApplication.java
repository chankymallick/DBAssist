package com.dbassist.dbassist;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("home-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 900, 600);

        // Add CSS stylesheet
        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());

        // Remove default window decorations for custom title bar
        stage.initStyle(StageStyle.UNDECORATED);


        stage.setTitle("DBAssist - Your AI-Powered Database Assistant");
        stage.setScene(scene);
        stage.setMinWidth(800);
        stage.setMinHeight(500);

        // Pass the stage to the controller for window controls
        HomeController controller = fxmlLoader.getController();
        controller.setStage(stage);

        // Launch in full screen (maximized) by default
        javafx.stage.Screen screen = javafx.stage.Screen.getPrimary();
        javafx.geometry.Rectangle2D bounds = screen.getVisualBounds();
        stage.setX(bounds.getMinX());
        stage.setY(bounds.getMinY());
        stage.setWidth(bounds.getWidth());
        stage.setHeight(bounds.getHeight());

        stage.show();
    }
}
