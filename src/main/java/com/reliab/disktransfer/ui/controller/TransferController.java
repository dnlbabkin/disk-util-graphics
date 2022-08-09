package com.reliab.disktransfer.ui.controller;

import com.google.api.services.drive.model.File;
import com.reliab.disktransfer.component.SceneCreator;
import com.reliab.disktransfer.service.GoogleService;
import com.reliab.disktransfer.service.TransferService;
import javafx.concurrent.WorkerStateEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;


@Slf4j
@Component
@FxmlView("/fxml/TransferPage.fxml")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;
    private final GoogleService googleService;
    private final SceneCreator pages;
    private final DirectoryChooserController directoryChooserController;
    private final ConfigurableApplicationContext context;


    @FXML
    public ProgressBar progressBar;
    @FXML
    public Button transfer;
    @FXML
    public Label numberOfFiles;
    @FXML
    public Button mainPage;


    @FXML
    public void initialize() {
        this.transfer.setOnAction(actionEvent -> fileTransfer());
        this.mainPage.setOnAction(actionEvent -> {
            transferService.cancel(true);
            if (transferService.isCancelled()) {
                log.info("task was cancelled");
            } else {
                log.info("task was not cancelled");
            }
            this.pages.switchToGoogleAuthPage(actionEvent);
        });
    }

    private void fileTransfer() {
        progressBar.progressProperty().unbind();
        progressBar.progressProperty().bind(transferService.progressProperty());
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
        Thread thread = new Thread(transferService);
        numberOfFiles.setAlignment(Pos.CENTER);
        numberOfFiles.textProperty().bind(transferService.messageProperty());
        transfer.setDisable(true);
        transferService.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, event -> successState());
        thread.start();
    }

    private void successState() {
        List<File> downloaded = transferService.getValue();
        numberOfFiles.textProperty().unbind();
        if (!Files.exists(Path.of(directoryChooserController.getDirectory()))) {
            numberOfFiles.setText("Файлы не передались, отсутствует директория");
        } else {
            numberOfFiles.setText("Выполнено: " + downloaded.size());
        }
        progressBar.progressProperty().unbind();
        progressBar.setProgress(0);
        transfer.setDisable(true);
    }
}
