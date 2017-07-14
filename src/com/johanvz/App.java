package com.johanvz;/**
 * Created by j on 13/07/2017.
 */

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class App extends Application {
    Controller controller;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("App.fxml"));
        try {
            Parent root = fxmlLoader.load();
            controller = fxmlLoader.getController();
            primaryStage.setTitle("LAN File Transfer");

            Scene scene = new Scene(root);

            controller.dropZone.setOnDragOver(event -> {
                Dragboard db = event.getDragboard();
                if (db.hasFiles()) {
                    event.acceptTransferModes(TransferMode.COPY);
                } else {
                    event.consume();
                }
            });

            // Dropping over surface
            controller.dropZone.setOnDragDropped(event -> {
                Dragboard db = event.getDragboard();
                boolean success = false;
                if (db.hasFiles()) {
                    success = true;
                    String filePath = null;
                    for (File file : db.getFiles()) {
                        filePath = file.getAbsolutePath();
                        System.out.println(filePath);
                    }
                }
                event.setDropCompleted(success);
                event.consume();
            });

            controller.menuBar.prefWidthProperty().bind(primaryStage.widthProperty());
            controller.tabPane.prefWidthProperty().bind(primaryStage.widthProperty());
            controller.lblDropFile.layoutXProperty().bind(primaryStage.widthProperty().subtract(controller.lblDropFile.widthProperty()).divide(2));

            primaryStage.setOnCloseRequest(event -> {
                Platform.exit();
                System.exit(0);
            });
            primaryStage.setScene(scene);

            primaryStage.show();
        } catch (
                IOException e)

        {
            e.printStackTrace();
        }

    }
}
