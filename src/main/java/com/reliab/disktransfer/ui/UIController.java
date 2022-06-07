package com.reliab.disktransfer.ui;

import com.reliab.disktransfer.service.AuthService;
import javafx.application.HostServices;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Component
public class UIController {

    private final HostServices hostServices;
    private final AuthService authService;

    public UIController(HostServices hostServices, AuthService authService) {
        this.hostServices = hostServices;
        this.authService = authService;
    }

    @FXML
    public Label label;

    @FXML
    public Button button;

    @FXML
    public Button googleAuth;

    @FXML
    public Button yandexAuth;

    @FXML
    public Button transfer;

//    @FXML
//    public void initialize(){
//        this.button.setOnAction(actionEvent -> this.label.setText(this.hostServices.getDocumentBase()));
//    }

    @FXML
    public void initialize(){
        this.button.setOnAction(actionEvent -> this.label.setText(this.hostServices.getDocumentBase()));
        this.googleAuth.setOnAction(actionEvent -> {
            try {
                this.authService.getFiles();
            } catch (GeneralSecurityException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
