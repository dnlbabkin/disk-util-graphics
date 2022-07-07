package com.reliab.disktransfer.ui.controller;

import com.reliab.disktransfer.component.SceneCreator;
import com.reliab.disktransfer.service.GoogleAuthService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import lombok.RequiredArgsConstructor;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Component;


@Component
@FxmlView("/fxml/GoogleAuthPage.fxml")
@RequiredArgsConstructor
public class GoogleAuthController {

    private final GoogleAuthService googleAuthService;
    private final SceneCreator pages;

    @FXML
    public Button googleAuth;


    @FXML
    public void initialize() {
        this.googleAuth.setOnAction(actionEvent -> {
            this.googleAuthService.getFileListFromGoogleDrive();
            this.pages.switchToYandexAuthPage(actionEvent);
        });
    }
}
