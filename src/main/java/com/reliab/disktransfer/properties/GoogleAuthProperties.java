package com.reliab.disktransfer.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "google-cred")
@Data
public class GoogleAuthProperties {
    private String credFilePath;
    private String googleTokensDirPath;
}
