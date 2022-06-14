package com.reliab.disktransfer.ui.controller;

import com.reliab.disktransfer.service.AuthService;
import javafx.concurrent.WorkerStateEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
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
    @FXML
    public final ProgressBar progressBar = new ProgressBar(0);
    @FXML
    public Label complete;
    @FXML
    public Button cancel;

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

        this.transfer.setOnAction(actionEvent -> {
            transfer.setDisable(true);
            progressBar.setProgress(0);
            progressBar.progressProperty().unbind();
            progressBar.progressProperty().bind(authService.progressProperty());
            authService.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED,
                    event -> {
                complete.textProperty().unbind();
                complete.setText("Complete");
            });
            new Thread(authService).start();
        });

        this.cancel.setOnAction(actionEvent -> {
            transfer.setDisable(false);
            cancel.setDisable(true);
            authService.cancel(true);
            progressBar.progressProperty().unbind();
            complete.textProperty().unbind();

            progressBar.setProgress(0);
        });
    }
}
