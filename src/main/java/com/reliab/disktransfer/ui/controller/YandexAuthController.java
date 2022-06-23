package com.reliab.disktransfer.ui.controller;

import com.reliab.disktransfer.service.AuthService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

@Component
@FxmlView("/fxml/YandexAuthPage.fxml")
@RequiredArgsConstructor
public class YandexAuthController {

    private final AuthService authService;
    private final ConfigurableApplicationContext context;

    @FXML
    public Button yandexAuth;

    @SneakyThrows
    private void switchToDirectoryChooser(ActionEvent actionEvent) {
        FxWeaver fxWeaver = context.getBean(FxWeaver.class);
        Parent root = fxWeaver.loadView(DirectoryChooserController.class);
        Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        Scene scene = new Scene(root, 513, 243);
        stage.setScene(scene);
        stage.show();
    }

    @SneakyThrows
    @FXML
    public void initialize() {
        this.yandexAuth.setOnAction(actionEvent -> {
            this.authService.browse();
            switchToDirectoryChooser(actionEvent);
        });
    }
}
