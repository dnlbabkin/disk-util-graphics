package com.reliab.disktransfer.ui;

import com.reliab.disktransfer.DiskTransferApplication;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

public class JavafxApplication extends Application {

    private ConfigurableApplicationContext context;

    public void browser(String url) {
        getHostServices().showDocument(url);
    }

    @Override
    public void init() {
        ApplicationContextInitializer<GenericApplicationContext> initializer =
                ac -> {
                    ac.registerBean(Application.class, () -> JavafxApplication.this);
                    ac.registerBean(Parameters.class, this::getParameters);
                    ac.registerBean(HostServices.class, this::getHostServices);
                };
        this.context = new SpringApplicationBuilder()
                .sources(DiskTransferApplication.class)
                .initializers(initializer)
                .run(getParameters().getRaw().toArray(new String[0]));
    }

    @Override
    public void start(Stage stage) {
        this.context.publishEvent(new StageReadyEvent(stage));
    }

    @Override
    public void stop() {
        this.context.close();
        Platform.exit();
    }
}

class StageReadyEvent extends ApplicationEvent {
    public Stage getStage(){
        return (Stage) getSource();
    }

    public StageReadyEvent(Stage source) {
        super(source);
    }
}