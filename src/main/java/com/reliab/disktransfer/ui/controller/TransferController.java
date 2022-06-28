package com.reliab.disktransfer.ui.controller;

import com.google.api.services.drive.model.File;
import com.reliab.disktransfer.service.AuthService;
import javafx.concurrent.WorkerStateEvent;
import javafx.fxml.FXML;
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

    private final AuthService authService;

    @FXML
    public ProgressBar progressBar;
    @FXML
    public Button transfer;
    @FXML
    public Label complete;

    private void fileTransfer() {
        progressBar.progressProperty().unbind();
        progressBar.progressProperty().bind(authService.progressProperty());

        complete.textProperty().bind(authService.messageProperty());

        authService.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED,
                event -> {
                    List<File> downloaded = authService.getValue();
                    complete.textProperty().unbind();
                    complete.setText("Выполнено: " + downloaded.size());
                });
        new Thread(authService).start();
    }

    @FXML
    public void initialize() {
        this.transfer.setOnAction(actionEvent -> fileTransfer());
    }
}
