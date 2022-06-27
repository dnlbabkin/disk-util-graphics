package com.reliab.disktransfer.ui.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

@Slf4j
@Component
@FxmlView("/fxml/DirectoryChooserPage.fxml")
@RequiredArgsConstructor
public class DirectoryChooserController {

    private final ConfigurableApplicationContext context;

    public String directory;

    @FXML
    public TextField field;
    @FXML
    public Button browse;
    @FXML
    public AnchorPane anchor;

    @SneakyThrows
    private void switchToTransfer(ActionEvent actionEvent) {
        FxWeaver fxWeaver = context.getBean(FxWeaver.class);
        Parent root = fxWeaver.loadView(TransferController.class);
        Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        Scene scene = new Scene(root, 513, 243);
        stage.setScene(scene);
        stage.show();
    }

    @SneakyThrows
    private void chooseFile() {
        final DirectoryChooser chooser = new DirectoryChooser();
        Stage window = (Stage) anchor.getScene().getWindow();
        File file = chooser.showDialog(window);

        if (file != null) {
            log.info("Path: " + file.getAbsolutePath());
            directory = file.getAbsolutePath();
            field.setText(directory);
        }

        BufferedWriter writer = new BufferedWriter(new FileWriter("src/main/resources/directory/"));
        writer.write(directory);
        writer.close();
    }

    @SneakyThrows
    @FXML
    public void initialize() {
        this.browse.setOnAction(actionEvent -> {
            chooseFile();
            switchToTransfer(actionEvent);
        });
    }
}
