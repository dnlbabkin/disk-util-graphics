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

    @SneakyThrows
    public void switchToYandexAuthPage(ActionEvent actionEvent) {
        FxWeaver fxWeaver = context.getBean(FxWeaver.class);
        Parent root = fxWeaver.loadView(YandexAuthController.class);
        Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        Scene scene = new Scene(root, 513, 243);
        stage.setScene(scene);
        stage.show();
    }

    @SneakyThrows
    public void switchToDirectoryChooser(ActionEvent actionEvent) {
        FxWeaver fxWeaver = context.getBean(FxWeaver.class);
        Parent root = fxWeaver.loadView(DirectoryChooserController.class);
        Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        Scene scene = new Scene(root, 513, 243);
        stage.setScene(scene);
        stage.show();
    }

    @SneakyThrows
    public void switchToTransfer(ActionEvent actionEvent) {
        FxWeaver fxWeaver = context.getBean(FxWeaver.class);
        Parent root = fxWeaver.loadView(TransferController.class);
        Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        Scene scene = new Scene(root, 513, 243);
        stage.setScene(scene);
        stage.show();
    }
}
