package com.reliab.disktransfer;

import com.reliab.disktransfer.ui.JavafxApplication;
import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan("com.reliab.disktransfer.configuration.properties")
@SpringBootApplication
public class DiskTransferApplication {

	public static void main(String[] args) {
		Application.launch(JavafxApplication.class, args);
	}

}
