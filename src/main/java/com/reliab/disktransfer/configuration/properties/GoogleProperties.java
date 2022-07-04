package com.reliab.disktransfer.configuration.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "google")
@Data
public class GoogleProperties {
    private String credFilePath;
    private String googleTokensDirPath;
    private String downloadFolderName;
    private String applicationName;
}
