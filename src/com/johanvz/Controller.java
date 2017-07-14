package com.johanvz;


import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Pane;


import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by j on 13/07/2017.
 */
public class Controller implements Initializable{
    @FXML
    public MenuBar menuBar;
    public MenuItem menuClose;
    public TabPane tabPane;
    public Pane dropZone;
    public Label lblDropFile;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public void menuEvent(ActionEvent actionEvent) {
        if(actionEvent.getSource() == menuClose) {
            Platform.exit();
            System.exit(0);
        }
    }

}
