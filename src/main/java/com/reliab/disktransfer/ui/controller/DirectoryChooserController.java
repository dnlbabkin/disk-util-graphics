package com.reliab.disktransfer.ui.controller;

import com.reliab.disktransfer.FxPages;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

@Slf4j
@Component
@FxmlView("/fxml/DirectoryChooserPage.fxml")
@RequiredArgsConstructor
public class DirectoryChooserController {

    private final FxPages pages;

    private String directory;

    @FXML
    public TextField field;
    @FXML
    public Button browse;
    @FXML
    public AnchorPane anchor;


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
           pages.switchToTransfer(actionEvent);
        });
    }
}
