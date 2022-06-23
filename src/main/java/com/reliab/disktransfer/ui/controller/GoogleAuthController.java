package com.reliab.disktransfer.ui.controller;

import com.reliab.disktransfer.service.AuthService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;


@Component
@FxmlView("/fxml/GoogleAuthPage.fxml")
@RequiredArgsConstructor
public class GoogleAuthController {

    private final AuthService authService;
    private final ConfigurableApplicationContext context;

    @FXML
    public Button googleAuth;

    @SneakyThrows
    private void switchToYandexAuthPage(ActionEvent actionEvent) {
        FxWeaver fxWeaver = context.getBean(FxWeaver.class);
        Parent root = fxWeaver.loadView(YandexAuthController.class);
        Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        Scene scene = new Scene(root, 513, 243);
        stage.setScene(scene);
        stage.show();
    }

    @SneakyThrows
    @FXML
    public void initialize() {
        this.googleAuth.setOnAction(actionEvent -> {
            authService.getFiles();
            switchToYandexAuthPage(actionEvent);
        });
    }
}
