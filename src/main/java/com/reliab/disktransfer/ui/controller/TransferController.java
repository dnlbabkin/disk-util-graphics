package com.reliab.disktransfer.ui.controller;

import com.google.api.services.drive.model.File;
import com.reliab.disktransfer.service.TransferService;
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

    private final TransferService transferService;

    @FXML
    public ProgressBar progressBar;
    @FXML
    public Button transfer;
    @FXML
    public Label complete;


    private void fileTransfer() {
        progressBar.progressProperty().unbind();
        progressBar.progressProperty().bind(transferService.progressProperty());

        complete.textProperty().bind(transferService.messageProperty());

        transferService.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED,
                event -> {
                    List<File> downloaded = transferService.getValue();
                    complete.textProperty().unbind();
                    complete.setText("Выполнено: " + downloaded.size());
                });
        new Thread(transferService).start();
    }

    @FXML
    public void initialize() {
        this.transfer.setOnAction(actionEvent -> fileTransfer());
    }
}
