package com.reliab.disktransfer.ui.controller;

import com.reliab.disktransfer.service.AuthService;
import javafx.concurrent.WorkerStateEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.List;

@Component
@RequiredArgsConstructor
public class UIController {

    private final AuthService authService;

    public String directory;

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
    @FXML
    public ProgressBar progressBar;
    @FXML
    public Label complete;
    @FXML
    public Button cancel;


    @SneakyThrows
    private void chooseFile() {
        final DirectoryChooser chooser = new DirectoryChooser();
        Stage window = (Stage) anchor.getScene().getWindow();
        File file = chooser.showDialog(window);

        if (file != null) {
            System.out.println("Path: " + file.getAbsolutePath());
            directory = file.getAbsolutePath();
            field.setText(directory);
        }

        BufferedWriter writer = new BufferedWriter(new FileWriter("src/main/resources/directory/"));
        writer.write(directory);
        writer.close();
    }

    private void cancel() {
        authService.cancel(true);
        progressBar.progressProperty().unbind();
        progressBar.setProgress(0);
        complete.textProperty().unbind();
    }

    private void fileTransfer() {
        progressBar.progressProperty().unbind();
        progressBar.progressProperty().bind(authService.progressProperty());

        complete.textProperty().bind(authService.messageProperty());

        authService.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED,
                event -> {
                    List<com.google.api.services.drive.model.File> downloaded = authService.getValue();
                    complete.textProperty().unbind();
                    complete.setText("Complete: " + downloaded.size());
                });
        new Thread(authService).start();
    }

    @FXML
    public void initialize() {
        this.googleauth.setOnAction(actionEvent -> this.authService.getFiles());
        this.yandexauth.setOnAction(actionEvent -> this.authService.browse());
        this.browse.setOnAction(actionEvent -> chooseFile());
        this.transfer.setOnAction(actionEvent -> fileTransfer());
        this.cancel.setOnAction(actionEvent -> cancel());
    }
}
