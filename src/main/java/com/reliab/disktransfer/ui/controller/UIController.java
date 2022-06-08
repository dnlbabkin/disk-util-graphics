package com.reliab.disktransfer.ui.controller;

import com.reliab.disktransfer.service.AuthService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.*;

@Component
@RequiredArgsConstructor
public class UIController {

    private final AuthService authService;

    @FXML
    public Button googleauth;
    @FXML
    public Button yandexauth;
    @FXML
    public Button transfer;
    @FXML
    public TextField field;
    @FXML
    public Button browse;
    @FXML
    public AnchorPane anchor;

    public String directory;

    @FXML
    public void initialize() {
        this.googleauth.setOnAction(actionEvent ->
            this.authService.getFiles()
        );

        this.yandexauth.setOnAction(actionEvent -> this.authService.browse());

        this.browse.setOnAction(actionEvent -> {
            final DirectoryChooser chooser = new DirectoryChooser();
            Stage window = (Stage) anchor.getScene().getWindow();
            File file = chooser.showDialog(window);

            if(file != null) {
                System.out.println("Path: " + file.getAbsolutePath());
                directory = file.getAbsolutePath();
                field.setText(directory);
            }

            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter("src/main/resources/directory/"));
                writer.write(directory);
                writer.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        this.transfer.setOnAction(actionEvent ->
            this.authService.fileTransfer()
        );
    }
}
