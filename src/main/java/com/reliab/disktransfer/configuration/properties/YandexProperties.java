package com.reliab.disktransfer.configuration.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@Data
@ConfigurationProperties(prefix = "yandex")
@ConfigurationPropertiesScan
public class YandexProperties {
    private String clientId;
    private String clientSecret;
    private String tokenUrl;
    private String redirectUri;
    private String yandexTokensDirPath;
}
