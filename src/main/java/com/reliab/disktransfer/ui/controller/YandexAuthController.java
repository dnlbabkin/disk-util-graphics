package com.reliab.disktransfer.ui.controller;

import com.reliab.disktransfer.component.SceneCreation;
import com.reliab.disktransfer.service.YandexAuthService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import lombok.RequiredArgsConstructor;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Component;

@Component
@FxmlView("/fxml/YandexAuthPage.fxml")
@RequiredArgsConstructor
public class YandexAuthController {

    private final YandexAuthService yandexAuthService;
    private final SceneCreation pages;

    @FXML
    public Button yandexAuth;

    @FXML
    public void initialize() {
        this.yandexAuth.setOnAction(actionEvent -> {
            this.yandexAuthService.browse();
            pages.switchToDirectoryChooser(actionEvent);
        });
    }
}
