package com.reliab.disktransfer.ui.controller;

import com.google.api.services.drive.model.File;
import com.reliab.disktransfer.service.GoogleService;
import com.reliab.disktransfer.service.TransferService;
import javafx.concurrent.WorkerStateEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import lombok.RequiredArgsConstructor;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@FxmlView("/fxml/TransferPage.fxml")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;
    private final GoogleService googleService;

    @FXML
    public ProgressBar progressBar;
    @FXML
    public Button transfer;
    @FXML
    public Label progress;
    @FXML
    public Label numberOfFiles;


    @FXML
    public void initialize() {
        this.transfer.setOnAction(actionEvent -> fileTransfer());
    }

    private void fileTransfer() {
        progressBar.progressProperty().unbind();
        progressBar.progressProperty().bind(transferService.progressProperty());
        numberOfFiles.textProperty().unbind();

        if(googleService.getFileList().isEmpty()) {
            actionIfDiskIsEmpty();
        } else {
            actionIfDiskIsNotEmpty();
        }
    }

    private void actionIfDiskIsEmpty() {
        numberOfFiles.setText("На диске нет файлов");
        numberOfFiles.setAlignment(Pos.CENTER);
        progressBar.progressProperty().unbind();
        progressBar.setProgress(0);
        transfer.setDisable(true);
    }

    private void actionIfDiskIsNotEmpty() {
        numberOfFiles.setText("Общее количество файлов на диске: " + googleService.getFileList().size());
        numberOfFiles.setAlignment(Pos.CENTER);
        progress.textProperty().bind(transferService.messageProperty());
        transfer.setDisable(true);
        transferService.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED,
                event -> {
                    List<File> downloaded = transferService.getValue();
                    progress.textProperty().unbind();
                    progress.setText("Выполнено: " + downloaded.size());
                    transfer.setDisable(true);
                });
        new Thread(transferService).start();
    }
}
