package com.reliab.disktransfer.ui.controller;

import com.reliab.disktransfer.service.AuthService;
import com.yandex.disk.rest.exceptions.ServerException;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.*;
import java.security.GeneralSecurityException;

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
    public void initialize(){
        this.googleauth.setOnAction(actionEvent -> {
            try {
                this.authService.getFiles();
            } catch (GeneralSecurityException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

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
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        this.transfer.setOnAction(actionEvent -> {
            try {
                this.authService.fileTransfer();
            } catch (GeneralSecurityException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (ServerException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
