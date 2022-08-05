package com.reliab.disktransfer.ui.controller;

import com.reliab.disktransfer.component.SceneCreator;
import com.reliab.disktransfer.configuration.properties.YandexProperties;
import com.reliab.disktransfer.service.YandexAuthService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Component;

import java.io.File;

@Slf4j
@Component
@FxmlView("/fxml/YandexAuthPage.fxml")
@RequiredArgsConstructor
public class YandexAuthController {

    private final YandexAuthService yandexAuthService;
    private final YandexProperties yandexProperties;
    private final SceneCreator pages;

    @FXML
    public Button yandexAuth;

    @FXML
    public void initialize() {
        this.yandexAuth.setOnAction(actionEvent -> {
            browse();
            this.pages.switchToDirectoryChooser(actionEvent);
        });
    }

    private void browse() {
        File tokenDirectoryPath = new File(yandexProperties.getYandexTokensDirPath());
        if (!tokenDirectoryPath.exists()) {
            this.yandexAuthService.browse();
        }
    }
}
