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
import net.rgielen.fxweaver.core.FxWeaver;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SceneCreation {

    private final ConfigurableApplicationContext context;

    private static final int WIDTH = 513;
    private static final int HEIGHT = 243;


    private void sceneCreation(ActionEvent actionEvent, Class<?> controllerClass) {
        FxWeaver fxWeaver = context.getBean(FxWeaver.class);
        Parent root = fxWeaver.loadView(controllerClass);
        Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        Scene scene = new Scene(root, WIDTH, HEIGHT);
        stage.setScene(scene);
        stage.show();
    }

    public void switchToYandexAuthPage(ActionEvent actionEvent) {
        sceneCreation(actionEvent, YandexAuthController.class);
    }

    public void switchToDirectoryChooser(ActionEvent actionEvent) {
        sceneCreation(actionEvent, DirectoryChooserController.class);
    }

    public void switchToTransfer(ActionEvent actionEvent) {
        sceneCreation(actionEvent, TransferController.class);
    }
}
