package com.reliab.disktransfer.ui.controller;

import com.reliab.disktransfer.component.SceneCreator;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Optional;

@Slf4j
@Component
@FxmlView("/fxml/DirectoryChooserPage.fxml")
@RequiredArgsConstructor
@Getter
public class DirectoryChooserController {

    private final SceneCreator pages;

    private String directory;

    @FXML
    public TextField field;
    @FXML
    public Button browse;
    @FXML
    public AnchorPane anchor;


    private void chooseFile() {
        final DirectoryChooser chooser = new DirectoryChooser();
        Stage window = (Stage) anchor.getScene().getWindow();
        File file = Optional.ofNullable(chooser.showDialog(window))
                .orElseThrow(() -> new RuntimeException("Directory wasn't chosen"));

        if (file != null) {
            log.info("Path: " + file.getAbsolutePath());
            directory = file.getAbsolutePath();
            field.setText(directory);
        }

        writeDirectoryToFile();
    }

    private void writeDirectoryToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("src/main/resources/directory/"))){
            writer.write(directory);
        } catch (IOException e) {
            log.warn("Directory cannot write to file");
        }
    }

    @FXML
    public void initialize() {
        this.browse.setOnAction(actionEvent -> {
            chooseFile();
            this.pages.switchToTransfer(actionEvent);
        });
    }
}
