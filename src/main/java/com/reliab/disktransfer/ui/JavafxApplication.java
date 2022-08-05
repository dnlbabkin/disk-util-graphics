package com.reliab.disktransfer.ui;

import com.reliab.disktransfer.DiskTransferApplication;
import com.reliab.disktransfer.ui.controller.GoogleAuthController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxWeaver;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;


@Slf4j
@Component
public class JavafxApplication extends Application {

    private ConfigurableApplicationContext context;

    public static final int WIDTH = 513;
    public static final int HEIGHT = 257;

    public void browser(String url)  {
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
        Scene scene = new Scene(root);
        stage.centerOnScreen();
        stage.setMaxHeight(HEIGHT);
        stage.setMaxWidth(WIDTH);
        stage.setMinHeight(HEIGHT);
        stage.setMinWidth(WIDTH);
        stage.setScene(scene);
        stage.setTitle("Disk Transfer App");
        actionOnCloseButton(stage);
        stage.show();
    }

    @Override
    public void stop() {
        this.context.close();
        Platform.exit();
    }

    private void actionOnCloseButton(Stage stage) {
        stage.setOnCloseRequest(windowEvent -> {
            File google = new File("src/main/resources/google-tokens");
            File yandex = new File("src/main/resources/yandex-tokens");
            try {
                FileUtils.deleteDirectory(new File(String.valueOf(google)));
            } catch (IOException e) {
                log.warn("Cannot find directory");
            }
            yandex.deleteOnExit();
            System.exit(0);
        });
    }
}