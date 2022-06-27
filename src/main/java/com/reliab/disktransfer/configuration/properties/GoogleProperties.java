package com.reliab.disktransfer.configuration.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationProperties(prefix = "google")
@Data
@ConfigurationPropertiesScan
public class GoogleProperties {
    private String credFilePath;
    private String googleTokensDirPath;
    private String downloadFolderName;
    private String applicationName;
}
