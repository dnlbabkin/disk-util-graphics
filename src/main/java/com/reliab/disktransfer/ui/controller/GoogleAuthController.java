package com.reliab.disktransfer.ui.controller;

import com.reliab.disktransfer.component.SceneCreation;
import com.reliab.disktransfer.service.AuthService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import lombok.RequiredArgsConstructor;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Component;


@Component
@FxmlView("/fxml/GoogleAuthPage.fxml")
@RequiredArgsConstructor
public class GoogleAuthController {

    private final AuthService authService;
    private final SceneCreation pages;

    @FXML
    public Button googleAuth;


    @FXML
    public void initialize() {
        this.googleAuth.setOnAction(actionEvent -> {
            authService.getFileListFromGoogleDrive();
            pages.switchToYandexAuthPage(actionEvent);
        });
    }
}
