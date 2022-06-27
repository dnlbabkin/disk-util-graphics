package com.reliab.disktransfer.component;

import com.reliab.disktransfer.ui.controller.DirectoryChooserController;
import com.reliab.disktransfer.ui.controller.TransferController;
import com.reliab.disktransfer.ui.controller.YandexAuthController;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.rgielen.fxweaver.core.FxWeaver;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FxPages {

    private final ConfigurableApplicationContext context;

    private static final int WIDTH = 0;
    private static final int HEIGHT = 0;

    @SneakyThrows
    private void sceneCreation(ActionEvent actionEvent, Class<?> controllerClass) {
        FxWeaver fxWeaver = context.getBean(FxWeaver.class);
        Parent root = fxWeaver.loadView(controllerClass);
        Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        Scene scene = new Scene(root, WIDTH, HEIGHT);
        stage.setScene(scene);
        stage.show();
    }

    @SneakyThrows
    public void switchToYandexAuthPage(ActionEvent actionEvent) {
        sceneCreation(actionEvent, YandexAuthController.class);
    }

    @SneakyThrows
    public void switchToDirectoryChooser(ActionEvent actionEvent) {
        sceneCreation(actionEvent, DirectoryChooserController.class);
    }

    @SneakyThrows
    public void switchToTransfer(ActionEvent actionEvent) {
        sceneCreation(actionEvent, TransferController.class);
    }
}
