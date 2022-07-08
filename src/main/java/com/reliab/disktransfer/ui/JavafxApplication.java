package com.reliab.disktransfer.ui;

import com.reliab.disktransfer.DiskTransferApplication;
import com.reliab.disktransfer.ui.controller.GoogleAuthController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import net.rgielen.fxweaver.core.FxWeaver;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

public class JavafxApplication extends Application {

    private ConfigurableApplicationContext context;

    public static final int WIDTH = 513;
    public static final int HEIGHT = 243;

    public void browser(String url) {
        getHostServices().showDocument(url);
    }

    @Override
    public void init() {
        String[] args = getParameters().getRaw().toArray(new String[0]);

        this.context = new SpringApplicationBuilder()
                .sources(DiskTransferApplication.class)
                .run(args);
    }

    @Override
    public void start(Stage stage) {
        FxWeaver fxWeaver = context.getBean(FxWeaver.class);
        Parent root = fxWeaver.loadView(GoogleAuthController.class);
        Scene scene = new Scene(root, WIDTH, HEIGHT);
        stage.setScene(scene);
        stage.setTitle("Disk Transfer App");
        stage.show();
    }

    @Override
    public void stop() {
        this.context.close();
        Platform.exit();
    }
}