package com.reliab.disktransfer;

import com.reliab.disktransfer.configuration.properties.GetTokenProperties;
import com.reliab.disktransfer.configuration.properties.GoogleAuthProperties;
import com.reliab.disktransfer.ui.JavafxApplication;
import javafx.application.Application;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties({GoogleAuthProperties.class,
		GetTokenProperties.class})
@SpringBootApplication
public class DiskTransferApplication {

	public static void main(String[] args) {
		Application.launch(JavafxApplication.class, args);
	}

}
